package loris.parfume.Services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.Requests.BranchesRequest;
import loris.parfume.DTOs.Requests.NearestBranchRequest;
import loris.parfume.DTOs.returnDTOs.BranchesDTO;
import loris.parfume.Models.Branches;
import loris.parfume.Models.Orders.DeliveryRates;
import loris.parfume.Models.Orders.Orders;
import loris.parfume.Repositories.BranchesRepository;
import loris.parfume.Repositories.Orders.DeliveryRatesRepository;
import loris.parfume.Repositories.Orders.OrdersRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static loris.parfume.DefaultEntitiesService.DELIVERY_RATE;

@Service
@RequiredArgsConstructor
public class BranchesService {

    private final BranchesRepository branchesRepository;
    private final DeliveryRatesRepository deliveryRatesRepository;
    private final OrdersRepository ordersRepository;
    private final CacheForAllService cacheForAllService;

    @CacheEvict(value = "branchesCache", allEntries = true)
    public Branches create(BranchesRequest branchesRequest) {

        Branches branch = Branches.builder()
                .createdTime(LocalDateTime.now())
                .name(branchesRequest.getName())
                .phone(branchesRequest.getPhone())
                .longitude(branchesRequest.getLongitude())
                .latitude(branchesRequest.getLatitude())
                .redirectTo(branchesRequest.getRedirectTo())
                .build();

        return branchesRepository.save(branch);
    }

    public List<Branches> all(String name) {

        if (name != null) {

            return branchesRepository.findAllByNameLikeIgnoreCase("%" + name + "%");
        }

        return cacheForAllService.allBranches();
    }

    public Branches getById(Long id) {

        return branchesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Branch Not Found"));
    }

    @CacheEvict(value = "branchesCache", allEntries = true)
    public Branches update(Long id, BranchesRequest branchesRequest) {

        Branches branch = getById(id);

        Optional.ofNullable(branchesRequest.getName()).ifPresent(branch::setName);
        Optional.ofNullable(branchesRequest.getPhone()).ifPresent(branch::setPhone);
        Optional.ofNullable(branchesRequest.getLongitude()).ifPresent(branch::setLongitude);
        Optional.ofNullable(branchesRequest.getLatitude()).ifPresent(branch::setLatitude);
        Optional.ofNullable(branchesRequest.getRedirectTo()).ifPresent(branch::setRedirectTo);

        return branchesRepository.save(branch);
    }

    @CacheEvict(value = "branchesCache", allEntries = true)
    public String delete(Long id) {

        Branches branch = getById(id);

        List<Orders> ordersList = ordersRepository.findAllByBranch(branch);
        List<Orders> batchUpdateOrdersList = new ArrayList<>();
        for (Orders order : ordersList) {

            order.setBranch(null);
            batchUpdateOrdersList.add(order);
        }
        ordersRepository.saveAll(batchUpdateOrdersList);

        branchesRepository.delete(branch);

        return "Branch Successfully Deleted";
    }

    double SEMI_MAJOR_AXIS_MT = 6378137;
    double SEMI_MINOR_AXIS_MT = 6356752.314245;
    double FLATTENING = 1 / 298.257223563;
    double ERROR_TOLERANCE = 1e-12;

    public double calculateDistance(double latitude1, double longitude1, double latitude2, double longitude2) {

        double U1 = Math.atan((1 - FLATTENING) * Math.tan(Math.toRadians(latitude1)));
        double U2 = Math.atan((1 - FLATTENING) * Math.tan(Math.toRadians(latitude2)));

        double sinU1 = Math.sin(U1);
        double cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2);
        double cosU2 = Math.cos(U2);

        double longitudeDifference = Math.toRadians(longitude2 - longitude1);
        double previousLongitudeDifference;

        double sinSigma, cosSigma, sigma, sinAlpha, cosSqAlpha, cos2SigmaM;

        do {
            sinSigma = Math.sqrt(Math.pow(cosU2 * Math.sin(longitudeDifference), 2) +
                    Math.pow(cosU1 * sinU2 - sinU1 * cosU2 * Math.cos(longitudeDifference), 2));
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * Math.cos(longitudeDifference);
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * Math.sin(longitudeDifference) / sinSigma;
            cosSqAlpha = 1 - Math.pow(sinAlpha, 2);
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
            if (Double.isNaN(cos2SigmaM)) {
                cos2SigmaM = 0;
            }
            previousLongitudeDifference = longitudeDifference;
            double C = FLATTENING / 16 * cosSqAlpha * (4 + FLATTENING * (4 - 3 * cosSqAlpha));
            longitudeDifference = Math.toRadians(longitude2 - longitude1) + (1 - C) * FLATTENING * sinAlpha *
                    (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * Math.pow(cos2SigmaM, 2))));
        } while (Math.abs(longitudeDifference - previousLongitudeDifference) > ERROR_TOLERANCE);

        double uSq = cosSqAlpha * (Math.pow(SEMI_MAJOR_AXIS_MT, 2) - Math.pow(SEMI_MINOR_AXIS_MT, 2)) / Math.pow(SEMI_MINOR_AXIS_MT, 2);

        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));

        double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * Math.pow(cos2SigmaM, 2))
                - B / 6 * cos2SigmaM * (-3 + 4 * Math.pow(sinSigma, 2)) * (-3 + 4 * Math.pow(cos2SigmaM, 2))));

        double distanceMt = SEMI_MINOR_AXIS_MT * A * (sigma - deltaSigma);
        return distanceMt / 1000;
    }

    public BranchesDTO getNearestBranch(NearestBranchRequest nearestBranchRequest) {

        List<Branches> branchesList = branchesRepository.findAll();

        if (branchesList.isEmpty()) {

            throw new IllegalStateException("No branches available");
        }

        Branches nearestBranch = findNearestBranch(nearestBranchRequest, branchesList);

        double distance = calculateDistance(nearestBranchRequest.getLongitude(), nearestBranchRequest.getLatitude(),
                nearestBranch.getLongitude(), nearestBranch.getLatitude());

        double sumForDelivery = calculateDeliverySum(nearestBranchRequest, nearestBranch);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#.00", symbols);

        double formattedDistance = Double.parseDouble(df.format(distance));
        double formattedSumForDelivery = Double.parseDouble(df.format(sumForDelivery));

        return new BranchesDTO(nearestBranch, formattedSumForDelivery, formattedDistance);
    }

    private Branches findNearestBranch(NearestBranchRequest request, List<Branches> branches) {

        Branches nearestBranch = null;
        double shortestDistance = Double.MAX_VALUE;

        for (Branches branch : branches) {

            double distance = calculateDistance(
                    branch.getLongitude(), branch.getLatitude(),
                    request.getLongitude(), request.getLatitude());

            if (distance < shortestDistance) {
                shortestDistance = distance;
                nearestBranch = branch;
            }
        }

        if (nearestBranch == null) {

            throw new EntityNotFoundException("No nearest branch found");
        }

        return nearestBranch;
    }

    public double calculateDeliverySum(NearestBranchRequest request, Branches branch) {

        DeliveryRates deliveryRate = deliveryRatesRepository.findFirstByIsActive(true);

        double distance = calculateDistance(request.getLongitude(), request.getLatitude(),
                branch.getLongitude(), branch.getLatitude());

        if (deliveryRate == null) {

            return DELIVERY_RATE.getSumPerKm() * distance;
        }

        if (deliveryRate.getIsFixed()) {

            return deliveryRate.getSumPerKm() * distance;
        }
        else {

            double distanceBeyondFreeKm = distance - deliveryRate.getFirstFreeKmQuantity();

            return Math.max(0, distanceBeyondFreeKm) * deliveryRate.getAfterFreeKmSumPerKm();
        }
    }
}