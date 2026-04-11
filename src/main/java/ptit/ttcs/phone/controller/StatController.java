package ptit.ttcs.phone.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ptit.ttcs.phone.dto.StatDTO;
import ptit.ttcs.phone.dto.StatRequest;
import ptit.ttcs.phone.service.StatService;

@RestController
@RequestMapping("/api/reports/statistics")
@RequiredArgsConstructor
public class StatController {

  private final StatService statService;
  
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<StatDTO> processStatRequest(
    @RequestBody @Valid StatRequest request
  ) {
    return ResponseEntity.ok(statService.getStat(request));
  }
}
