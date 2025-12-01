package ng.com.createsoftware.fn_branch_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_branch_service.model.Branch;
import ng.com.createsoftware.fn_branch_service.repository.BranchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchServiceImpl implements BranchService{
    private final BranchRepository branchRepository;

    @Override
    public Branch addBranch(Branch branch) {
        return branchRepository.save(branch);
    }

    @Override
    public List<Branch> listBranches() {
        return branchRepository.findAll();
    }
}
