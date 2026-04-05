package ptit.ttcs.phone.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ptit.ttcs.phone.dto.BrandResponse;
import ptit.ttcs.phone.service.HomePageService;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

  private final HomePageService homePageService;

  @GetMapping
  public ResponseEntity<List<BrandResponse>> getBrands() {
    return ResponseEntity.ok(homePageService.getBrands());
  }
}
