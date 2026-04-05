package ptit.ttcs.phone.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ptit.ttcs.phone.dto.HomePageResponse;
import ptit.ttcs.phone.service.HomeService;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@Tag(name = "Home", description = "API cho trang chủ")
public class HomeController {
  private final HomeService homeService;

  @GetMapping
  @Operation(summary = "Xem trang chủ", description = "Lấy dữ liệu tổng hợp cho màn hình trang chủ")
  public ResponseEntity<HomePageResponse> getHomePage() {
    return ResponseEntity.ok(homeService.getHomePage());
  }
}