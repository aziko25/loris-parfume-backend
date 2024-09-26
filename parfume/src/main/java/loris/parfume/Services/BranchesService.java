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
import org.json.JSONObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static loris.parfume.DefaultEntitiesService.DELIVERY_RATE;

@Service
@RequiredArgsConstructor
public class BranchesService {

    private final BranchesRepository branchesRepository;
    private final DeliveryRatesRepository deliveryRatesRepository;
    private final OrdersRepository ordersRepository;
    private final CacheForAllService cacheForAllService;
    private final JdbcTemplate jdbcTemplate;

    @CacheEvict(value = "branchesCache", allEntries = true)
    public Branches create(BranchesRequest branchesRequest) {

        Branches branch = Branches.builder()
                .createdTime(LocalDateTime.now())
                .name(branchesRequest.getName())
                .phone(branchesRequest.getPhone())
                .longitude(branchesRequest.getLongitude())
                .latitude(branchesRequest.getLatitude())
                .redirectTo(branchesRequest.getRedirectTo())
                .sortOrder(branchesRequest.getSortOrder())
                .tgChatId(branchesRequest.getTgChatId())
                .city(branchesRequest.getCity())
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
        Optional.ofNullable(branchesRequest.getSortOrder()).ifPresent(branch::setSortOrder);
        Optional.ofNullable(branchesRequest.getTgChatId()).ifPresent(branch::setTgChatId);
        Optional.ofNullable(branchesRequest.getCity()).ifPresent(branch::setCity);

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

        return (distanceMt / 1000) + 1.5;
    }

    public double getRoadDistance(double userLat, double userLon, double branchLat, double branchLon) {

        try {

            int timeout = 3000;

            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(timeout);
            requestFactory.setReadTimeout(timeout);

            RestTemplate restTemplate = new RestTemplate(requestFactory);

            // Construct the OSRM URL
            String osrmUrl = String.format(Locale.US,
                    "http://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=false",
                    userLat, userLon, branchLat, branchLon);
            System.out.println(osrmUrl);

            // Make the request
            String response = restTemplate.getForObject(osrmUrl, String.class);
            System.out.println("OSRM Response: " + response);

            // Parse the response
            JSONObject jsonObject = new JSONObject(response);
            double distanceInMeters = jsonObject.getJSONArray("routes")
                    .getJSONObject(0)
                    .getDouble("distance");

            // Return the distance in kilometers
            return distanceInMeters / 1000;
        } catch (Exception e) {
            // Handle any exception (including timeout) by returning 0
            System.out.println("Error: " + e.getMessage());
            return 0;
        }
    }

    public BranchesDTO getNearestBranch(NearestBranchRequest nearestBranchRequest) {

        List<Branches> branchesList = cacheForAllService.allBranches();

        if (branchesList.isEmpty()) {
            branchesList = branchesRepository.findAll();
        }

        Branches nearestBranch = findNearestBranch(nearestBranchRequest, branchesList);

        double distance = getRoadDistance(nearestBranchRequest.getLatitude(), nearestBranchRequest.getLongitude(),
                nearestBranch.getLatitude(), nearestBranch.getLongitude());

        if (distance == 0) {
            distance = calculateDistance(nearestBranchRequest.getLatitude(), nearestBranchRequest.getLongitude(),
                    nearestBranch.getLatitude(), nearestBranch.getLongitude());
            distance += 1.2;
        }

        double sumForDelivery = calculateDeliverySum(nearestBranchRequest, nearestBranch, distance, nearestBranchRequest.getCity());

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#.00", symbols);

        double formattedDistance = Double.parseDouble(df.format(distance));
        double formattedSumForDelivery = Double.parseDouble(df.format(sumForDelivery));

        return new BranchesDTO(nearestBranch, formattedSumForDelivery, formattedDistance);
    }

    private boolean isInOtherRegion(String city) {

        return !city.equalsIgnoreCase("tashkent") && !city.equalsIgnoreCase("toshkent")
                && !city.equalsIgnoreCase("ташкент");
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

    public double calculateDeliverySum(NearestBranchRequest request, Branches branch, Double distance, String city) {

        if (isInOtherRegion(city)) {

            return 30000.0;
        }

        DeliveryRates deliveryRate = deliveryRatesRepository.findFirstByIsActive(true);

        if (distance == null) {
            distance = getRoadDistance(request.getLatitude(), request.getLongitude(),
                    branch.getLatitude(), branch.getLongitude());

            if (distance == 0) {
                distance = calculateDistance(request.getLatitude(), request.getLongitude(),
                        branch.getLatitude(), branch.getLongitude());
                distance += 1.2;
            }
        }

        if (deliveryRate == null || deliveryRate.getIsFixed()) {

            return Math.floor(DELIVERY_RATE.getSumPerKm() * distance);
        }

        if (deliveryRate.getFirstFreeKmQuantity() != null) {

            double distanceBeyondFreeKm = Math.max(0, distance - deliveryRate.getFirstFreeKmQuantity());

            return Math.floor(distanceBeyondFreeKm * deliveryRate.getAfterFreeKmSumPerKm());
        }

        if (deliveryRate.getFirstPaidKmQuantity() != null) {

            double initialKmSum = Math.min(distance, deliveryRate.getFirstPaidKmQuantity()) *
                    deliveryRate.getFirstPaidKmQuantityPrice();

            double extraDistance = Math.max(0, distance - deliveryRate.getFirstPaidKmQuantity());
            double additionalKmSum = extraDistance * deliveryRate.getAfterPaidKmSumPerKm();

            return Math.floor(initialKmSum + additionalKmSum);
        }

        throw new IllegalArgumentException("Problems With Delivery Rate!");
    }
}