package com.hostelhub.repository;

import com.hostelhub.model.GroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {
    List<GroupMembership> findByGroupId(Long groupId);
    List<GroupMembership> findByUserId(Long userId);
    boolean existsByUserIdAndGroupId(Long userId, Long groupId);
    Optional<GroupMembership> findByUserIdAndGroupId(Long userId, Long groupId);
    void deleteByUserIdAndGroupId(Long userId, Long groupId);
    void deleteByGroupId(Long groupId);
}
