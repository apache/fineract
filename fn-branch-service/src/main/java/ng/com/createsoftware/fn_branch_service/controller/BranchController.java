package ng.com.createsoftware.fn_branch_service;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_branch_service.model.Branch;
import ng.com.createsoftware.fn_branch_service.service.BranchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {
    private final BranchService branchService;

    @GetMapping
    public ResponseEntity<List<Branch>> listBranchesHandler(){
        return new ResponseEntity<>(branchService.listBranches(), HttpStatus.OK);
    }

    @PostMapping
    public  ResponseEntity<Branch> addBranchHandler(@RequestBody Branch branch){
        return new ResponseEntity<>(branchService.addBranch(branch), HttpStatus.CREATED);
    }
}
