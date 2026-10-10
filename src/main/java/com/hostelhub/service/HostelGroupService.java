package com.hostelhub.service;

import com.hostelhub.dto.CreateGroupRequest;
import com.hostelhub.dto.GroupResponse;
import com.hostelhub.dto.UpdateGroupRequest;
import com.hostelhub.exception.GroupAlreadyExistsException;
import com.hostelhub.exception.GroupNotFoundException;
import com.hostelhub.model.HostelGroup;
import com.hostelhub.repository.GroupMembershipRepository;
import com.hostelhub.repository.HostelGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HostelGroupService {

    private final HostelGroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;

    public HostelGroupService(HostelGroupRepository groupRepository, GroupMembershipRepository membershipRepository) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
    }

    public GroupResponse createGroup(CreateGroupRequest request) {
        if (groupRepository.existsBySectionCode(request.getSectionCode())) {
            throw new GroupAlreadyExistsException("Group already exists with section code: " + request.getSectionCode());
        }

        HostelGroup group = new HostelGroup();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setSectionCode(request.getSectionCode());

        HostelGroup saved = groupRepository.save(group);
        return mapToGroupResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(this::mapToGroupResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroupById(Long id) {
        HostelGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + id));
        return mapToGroupResponse(group);
    }

    public GroupResponse updateGroup(Long id, UpdateGroupRequest request) {
        HostelGroup existing = groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + id));

        if (!existing.getSectionCode().equals(request.getSectionCode()) &&
                groupRepository.existsBySectionCode(request.getSectionCode())) {
            throw new GroupAlreadyExistsException("Group already exists with section code: " + request.getSectionCode());
        }

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setSectionCode(request.getSectionCode());

        HostelGroup updated = groupRepository.save(existing);
        return mapToGroupResponse(updated);
    }

    public void deleteGroup(Long id) {
        if (!groupRepository.existsById(id)) {
            throw new GroupNotFoundException("Group not found with ID: " + id);
        }
        // Safely remove existing memberships first to prevent foreign key issues
        membershipRepository.deleteByGroupId(id);
        groupRepository.deleteById(id);
    }

    private GroupResponse mapToGroupResponse(HostelGroup group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getSectionCode(),
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }
}
