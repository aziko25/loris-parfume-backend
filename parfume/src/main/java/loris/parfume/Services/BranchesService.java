package loris.parfume.Services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.Requests.BranchesRequest;
import loris.parfume.DTOs.Requests.NearestBranchRequest;
import loris.parfume.DTOs.returnDTOs.BranchesDTO;
import loris.parfume.Models.Branches;
import loris.parfume.Models.Orders.DeliveryRates;
import loris.parfume.Repositories.BranchesRepository;
import loris.parfume.Repositories.Orders.DeliveryRatesRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static loris.parfume.Services.Orders.DeliveryRatesService.DELIVERY_RATE;

@Service
@RequiredArgsConstructor
public class BranchesService {

    private final BranchesRepository branchesRepository;
    private final DeliveryRatesRepository deliveryRatesRepository;

    @Value("${pageSize}")
    private Integer pageSize;

    public Branches create(BranchesRequest branchesRequest) {

        Branches branch = Branches.builder()
                .name(branchesRequest.getName())
                .longitude(branchesRequest.getLongitude())
                .latitude(branchesRequest.getLatitude())
                .redirectTo(branchesRequest.getRedirectTo())
                .build();

        return branchesRepository.save(branch);
    }

    public Page<Branches> all(Integer page, String name) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("name"));

        if (name != null) {

            return branchesRepository.findAllByNameLikeIgnoreCase("%" + name + "%", pageable);
        }

        return branchesRepository.findAll(pageable);
    }

    public Branches getById(Long id) {

        return branchesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Branch Not Found"));
    }

    public Branches update(Long id, BranchesRequest branchesRequest) {

        Branches branch = getById(id);

        Optional.ofNullable(branchesRequest.getName()).ifPresent(branch::setName);
        Optional.ofNullable(branchesRequest.getLongitude()).ifPresent(branch::setLongitude);
        Optional.ofNullable(branchesRequest.getLatitude()).ifPresent(branch::setLatitude);
        Optional.ofNullable(branchesRequest.getRedirectTo()).ifPresent(branch::setRedirectTo);

        return branchesRepository.save(branch);
    }

    public String delete(Long id) {

        Branches branch = getById(id);

        branchesRepository.delete(branch);

        return "Branch Successfully Deleted";
    }

    public double calculateDistance(double lon1, double lat1, double lon2, double lat2) {

        final int R = 6371; // Radius of the Earth in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                + Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in kilometers
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

        return new BranchesDTO(nearestBranch, sumForDelivery, distance);
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

    private double calculateDeliverySum(NearestBranchRequest request, Branches branch) {

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