package ng.com.createsoftware.fn_asset_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name="assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String assetName;
    private BigDecimal value;

    @ManyToOne
    @JoinColumn(name="asset_type_id")
    private AssetType type;

    private String branchCode;

    private Long clientId;///fineract client
    private String status;
}
