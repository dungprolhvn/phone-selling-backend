package ptit.ttcs.phone.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.antlr.v4.runtime.tree.Tree;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Data
public class Cart {
  
  @JsonIgnore // marked for ignore
  private static final int MAX_COUNT_PER_PRODUCT = 5;
  
  @Size(min = 1, message = "Gio hang khong duoc de trong")
  Map<Integer, Integer> products = new HashMap<>();
  // map<productId, count>, count can be negative (in case of removing item from cart)
  
  public void update(int productId, int count) throws IllegalArgumentException {
    products.put(productId, products.getOrDefault(productId, 0) + count);
    int newQuantity = products.get(productId);
    if (newQuantity < 0 || newQuantity > MAX_COUNT_PER_PRODUCT) {
      throw new IllegalArgumentException("So luong san pham them vao gio khong hop le");
    }
    if (newQuantity == 0) {
      products.remove(productId);
    }
  }
  
  public void merge(Cart guestCart) {
    if (guestCart.getProducts() != null) {
      guestCart.getProducts().forEach((productId, count) -> {
        update(productId, count);
      });
    }
  }
  
  public TreeMap<Integer, Integer> getSortedProducts() {
    return new TreeMap<>(products);
  }
}
