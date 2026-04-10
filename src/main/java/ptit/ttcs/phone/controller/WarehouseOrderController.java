package ptit.ttcs.phone.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ptit.ttcs.phone.entity.Order;
import ptit.ttcs.phone.dto.OrderResponse;
import ptit.ttcs.phone.dto.WarehouseOrderStatusUpdateRequest;
import ptit.ttcs.phone.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class WarehouseOrderController {

	private final OrderService orderService;

	@PreAuthorize("hasRole('WAREHOUSE_STAFF')")
	@GetMapping("/{orderId}")
	public ResponseEntity<Order> getOrderById(@PathVariable Integer orderId) {
		return ResponseEntity.ok(orderService.getOrderById(orderId));
	}

	@PreAuthorize("hasRole('WAREHOUSE_STAFF')")
	@PostMapping("/statusUpdate")
	public ResponseEntity<OrderResponse> updateOrderStatus(
			@RequestBody @Valid WarehouseOrderStatusUpdateRequest request) {
		return ResponseEntity.ok(orderService.updateOrderStatusByWarehouse(request));
	}

    
}
