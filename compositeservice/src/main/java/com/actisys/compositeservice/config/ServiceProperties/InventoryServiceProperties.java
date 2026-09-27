package com.actisys.compositeservice.config.ServiceProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "inventoryservice")
@Getter
@Setter
public class InventoryServiceProperties {
  private String host;
  private Endpoints endpoints = new Endpoints();

  @Getter
  @Setter
  public static class Endpoints {
    private String addNewRecipe;
    private String deleteRecipeById;
    private String getRecipeByDishId;
  }
}
