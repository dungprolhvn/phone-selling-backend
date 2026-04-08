package ptit.ttcs.phone.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ptit.ttcs.phone.dto.ShippingAddressRequest;
import ptit.ttcs.phone.dto.ShippingAddressResponse;
import ptit.ttcs.phone.entity.Account;
import ptit.ttcs.phone.entity.ShippingAddress;
import ptit.ttcs.phone.exception.NotFoundException;
import ptit.ttcs.phone.repository.AccountRepository;
import ptit.ttcs.phone.repository.ShippingAddressRepository;

@Service
@RequiredArgsConstructor
public class ShippingAddressService {
  private final ShippingAddressRepository shippingAddressRepository;
  private final AccountRepository accountRepository;

  public List<ShippingAddressResponse> getAddresses(Integer userId) {
    return shippingAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public List<ShippingAddressResponse> createAddress(Integer userId, ShippingAddressRequest request) {
    Account user = accountRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));

    ShippingAddress address = new ShippingAddress();
    address.setUser(user);
    address.setRecipientName(request.getRecipientName().trim());
    address.setRecipientPhone(request.getRecipientPhone().trim());
    address.setAddress(request.getAddress().trim());
    address.setCreatedAt(Instant.now());

    boolean isFirstAddress = shippingAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId).isEmpty();
    boolean shouldSetDefault = Boolean.TRUE.equals(request.getIsDefault()) || isFirstAddress;

    if (shouldSetDefault) {
      shippingAddressRepository.clearDefaultByUserId(userId);
    }
    address.setIsDefault(shouldSetDefault);

    shippingAddressRepository.save(address);
    return getAddresses(userId);
  }

  @Transactional
  public List<ShippingAddressResponse> updateAddress(Integer userId, Integer addressId, ShippingAddressRequest request) {
    ShippingAddress address = shippingAddressRepository.findByIdAndUserId(addressId, userId)
        .orElseThrow(() -> new NotFoundException("Shipping address not found"));

    address.setRecipientName(request.getRecipientName().trim());
    address.setRecipientPhone(request.getRecipientPhone().trim());
    address.setAddress(request.getAddress().trim());

    if (Boolean.TRUE.equals(request.getIsDefault())) {
      shippingAddressRepository.clearDefaultByUserId(userId);
      address.setIsDefault(true);
    }

    shippingAddressRepository.save(address);
    return getAddresses(userId);
  }

  @Transactional
  public List<ShippingAddressResponse> deleteAddress(Integer userId, Integer addressId) {
    ShippingAddress address = shippingAddressRepository.findByIdAndUserId(addressId, userId)
        .orElseThrow(() -> new NotFoundException("Shipping address not found"));

    boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
    shippingAddressRepository.delete(address);

    if (wasDefault) {
      List<ShippingAddress> remaining = shippingAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
      if (!remaining.isEmpty()) {
        shippingAddressRepository.clearDefaultByUserId(userId);
        ShippingAddress nextDefault = remaining.get(0);
        nextDefault.setIsDefault(true);
        shippingAddressRepository.save(nextDefault);
      }
    }

    return getAddresses(userId);
  }

  private ShippingAddressResponse toResponse(ShippingAddress address) {
    return new ShippingAddressResponse(
        address.getId(),
        address.getRecipientName(),
        address.getRecipientPhone(),
        address.getAddress(),
        address.getIsDefault(),
        address.getCreatedAt()
    );
  }
}
