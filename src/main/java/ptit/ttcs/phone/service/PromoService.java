package ptit.ttcs.phone.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptit.ttcs.phone.dto.PromoCreationRequest;
import ptit.ttcs.phone.entity.Product;
import ptit.ttcs.phone.entity.Promo;
import ptit.ttcs.phone.entity.PromoEligibility;
import ptit.ttcs.phone.entity.PromoEligibilityId;
import ptit.ttcs.phone.repository.PromoEligibilityRepository;
import ptit.ttcs.phone.repository.PromoRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PromoService {
  private final PromoRepository promoRepository;
  private final PromoEligibilityRepository promoEligibilityRepository;
  
  public @Nullable List<Promo> getAllPromos() {
    return promoRepository.findAll();
  }
  
  @Transactional
  public @Nullable Void createPromo(@Valid PromoCreationRequest request) {
    // Map PromoCreationRequest to Promo entity
    Promo promo = new Promo();
    promo.setName(request.getName());
    promo.setDescription(request.getDescription());
    promo.setStartDate(request.getStartDate());
    promo.setEndDate(request.getEndDate());
    promo.setDiscountType(request.getDiscountType());
    promo.setDiscountValue(BigDecimal.valueOf(request.getDiscountValue()));
    promo.setVoucherCode(request.getVoucherCode());
    promo.setUsageLimit(request.getUsageLimit());
    promo.setUsageCount(0);
    promo.setPromoImageUrls(request.getPromoImageUrls());
    promo.setCreatedAt(Instant.now());
    promo.setUpdatedAt(Instant.now());
    
    promo = promoRepository.save(promo);
    
    for (Integer productId : request.getProductIds()) {
      PromoEligibility promoEligibility = getPromoEligibility(productId, promo);
      promoEligibilityRepository.save(promoEligibility);
    }
    
    log.info("Promo and eligibilities saved successfully.");
    return null;
  }
  
  private PromoEligibility getPromoEligibility(Integer productId, Promo promo) {
    PromoEligibility promoEligibility = new PromoEligibility();
    PromoEligibilityId promoEligibilityId = new PromoEligibilityId();
    promoEligibilityId.setPromoId(promo.getId());
    promoEligibilityId.setProductId(productId);
    
    promoEligibility.setId(promoEligibilityId);
    promoEligibility.setPromo(promo);
    
    // Assuming you have a Product entity and repository
    Product product = new Product();
    product.setId(productId);
    promoEligibility.setProduct(product);
    return promoEligibility;
  }
  
  public @Nullable List<Promo> getOngoingPromos() {
    return promoRepository.findAllByEndDateAfter(Instant.now());
  }
}
