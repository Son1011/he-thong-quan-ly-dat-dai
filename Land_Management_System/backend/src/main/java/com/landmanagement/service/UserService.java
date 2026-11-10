package com.landmanagement.service;

import com.landmanagement.dto.request.UserCreateRequest;
import com.landmanagement.dto.request.UserUpdateRequest;
import com.landmanagement.dto.response.UserResponse;
import com.landmanagement.entity.AdministrativeUnit;
import com.landmanagement.entity.UserAccount;
import com.landmanagement.enums.UserRole;
import com.landmanagement.enums.UnitLevel;
import com.landmanagement.repository.UnitRepository;
import com.landmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UnitRepository unitRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (request.getUnitId() == null) {
            throw new IllegalArgumentException("Unit ID is required");
        }

        AdministrativeUnit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị"));

        validateRoleUnitMatch(unit, request.getRole());

        UserAccount user = UserAccount.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(request.getRole())
                .unitId(request.getUnitId())
                .mustChangePassword(Boolean.TRUE.equals(request.getMustChangePassword()))
                .isActive(true)
                .build();

        // Nếu không bắt đổi mật khẩu (mustChangePassword=false) thì khởi tạo các
        // timestamp
        if (!Boolean.TRUE.equals(user.getMustChangePassword())) {
            LocalDateTime now = LocalDateTime.now();
            user.setPasswordChangedAt(now);
            user.setPasswordExpiresAt(now.plusDays(90));
        }

        UserAccount saved = userRepository.save(user);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsersWithPagination(String q, UserRole role, Long unitId, int page, int size) {
        String query = q == null || q.isBlank() ? null : q.trim();

        Page<UserAccount> users = userRepository.searchUsers(
                query,
                role,
                unitId,
                PageRequest.of(page, size));

        return users.map(this::toResponse);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        UserRole targetRole = request.getRole() != null ? request.getRole() : user.getRole();
        Long targetUnitId = request.getUnitId() != null ? request.getUnitId() : user.getUnitId();

        if (request.getRole() != null || request.getUnitId() != null) {
            if (targetUnitId == null) {
                throw new IllegalArgumentException("Unit ID is required to validate role");
            }

            AdministrativeUnit unit = unitRepository.findById(targetUnitId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị"));

            validateRoleUnitMatch(unit, targetRole);
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getUnitId() != null) {
            user.setUnitId(request.getUnitId());
        }

        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        if (request.getMustChangePassword() != null) {
            user.setMustChangePassword(request.getMustChangePassword());
        }

        UserAccount updated = userRepository.save(user);

        return toResponse(updated);
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }

        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới tối thiểu 6 ký tự");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);

        // Cập nhật thời điểm thay đổi mật khẩu và thời hạn hết hạn (90 ngày)
        LocalDateTime now = LocalDateTime.now();
        user.setPasswordChangedAt(now);
        user.setPasswordExpiresAt(now.plusDays(90));

        userRepository.save(user);
    }

    private void validateRoleUnitMatch(AdministrativeUnit unit, UserRole role) {
        if (role == UserRole.CENTRAL_OFFICER && unit.getUnitLevel() != UnitLevel.CENTRAL) {
            throw new IllegalArgumentException("Quyền (vai trò) không phù hợp với cấp đơn vị");
        }

        if (role == UserRole.PROVINCE_OFFICER && unit.getUnitLevel() != UnitLevel.PROVINCE) {
            throw new IllegalArgumentException("Quyền (vai trò) không phù hợp với cấp đơn vị");
        }

        if (role == UserRole.COMMUNE_OFFICER && unit.getUnitLevel() != UnitLevel.COMMUNE) {
            throw new IllegalArgumentException("Quyền (vai trò) không phù hợp với cấp đơn vị");
        }
    }

    private UserResponse toResponse(UserAccount user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .unitId(user.getUnitId())
                .isActive(user.getIsActive())
                .mustChangePassword(user.getMustChangePassword())
                .build();
    }
}