package ng.com.createsoftware.fn_asset_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Double value;

    @ManyToOne
    @JoinColumn(name="asset_type_id")
    private AssetType type;

    private String branchCode;

    private String status;
}
