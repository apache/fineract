package ng.com.createsoftware.fn_branch_service.service;

import ng.com.createsoftware.fn_branch_service.model.Branch;

import java.util.List;

public interface BranchService {
    Branch addBranch(Branch branch);
    List<Branch> listBranches();
}
