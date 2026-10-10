package com.hostelhub.service;

import com.hostelhub.dto.GroupMemberResponse;
import com.hostelhub.dto.GroupResponse;
import com.hostelhub.exception.GroupNotFoundException;
import com.hostelhub.exception.MembershipAlreadyExistsException;
import com.hostelhub.exception.MembershipNotFoundException;
import com.hostelhub.exception.UserNotFoundException;
import com.hostelhub.model.GroupMembership;
import com.hostelhub.model.HostelGroup;
import com.hostelhub.model.User;
import com.hostelhub.repository.GroupMembershipRepository;
import com.hostelhub.repository.HostelGroupRepository;
import com.hostelhub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GroupMembershipService {

    private final GroupMembershipRepository membershipRepository;
    private final HostelGroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupMembershipService(GroupMembershipRepository membershipRepository,
                                  HostelGroupRepository groupRepository,
                                  UserRepository userRepository) {
        this.membershipRepository = membershipRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public GroupMemberResponse addUserToGroup(Long groupId, Long userId) {
        HostelGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        if (membershipRepository.existsByUserIdAndGroupId(userId, groupId)) {
            throw new MembershipAlreadyExistsException("User is already a member of this group");
        }

        GroupMembership membership = new GroupMembership();
        membership.setGroup(group);
        membership.setUser(user);

        GroupMembership saved = membershipRepository.save(membership);
        return mapToGroupMemberResponse(saved);
    }

    public void removeUserFromGroup(Long groupId, Long userId) {
        if (!groupRepository.existsById(groupId)) {
            throw new GroupNotFoundException("Group not found with ID: " + groupId);
        }
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        if (!membershipRepository.existsByUserIdAndGroupId(userId, groupId)) {
            throw new MembershipNotFoundException("User is not a member of this group");
        }

        membershipRepository.deleteByUserIdAndGroupId(userId, groupId);
    }

    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupMembers(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new GroupNotFoundException("Group not found with ID: " + groupId);
        }

        return membershipRepository.findByGroupId(groupId).stream()
                .map(this::mapToGroupMemberResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getUserGroups(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        return membershipRepository.findByUserId(userId).stream()
                .map(membership -> mapToGroupResponse(membership.getGroup()))
                .collect(Collectors.toList());
    }

    private GroupMemberResponse mapToGroupMemberResponse(GroupMembership membership) {
        User user = membership.getUser();
        return new GroupMemberResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                membership.getJoinedAt()
        );
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
