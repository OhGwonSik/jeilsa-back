package com.common.auth.organization.service;


import com.common.auth.common.util.SecurityUtil;
import com.common.auth.organization.domain.Organization;
import com.common.auth.organization.dto.CreateOrganizationRequest;
import com.common.auth.organization.dto.OrganizationResponse;
import com.common.auth.organization.dto.UpdateOrganizationRequest;
import com.common.auth.organization.mapper.OrganizationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {
    //----- DI Fields -----//
    private final OrganizationMapper organizationMapper;

    public Optional<Organization> findById(Integer organizationId) {
        log.debug("Finding organization by ID: {}", organizationId);
        return organizationMapper.selectOrganizationByOrganizationId(organizationId);
    }

    public Optional<Organization> findByName(String organizationName) {
        log.debug("Finding organization by name: {}", organizationName);
        return organizationMapper.selectOrganizationByOrganizationName(organizationName);
    }

    public List<Organization> findAll() {
        log.debug("Finding all organizations");
        return organizationMapper.selectAllOrganizations();
    }

    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        log.info("Creating new organization with name: {}", request.getOrganizationName());
        
        if (organizationMapper.selectOrganizationByOrganizationName(request.getOrganizationName()).isPresent()) {
            throw new IllegalArgumentException("Organization with name " + request.getOrganizationName() + " already exists");
        }

        Integer currentUserId = SecurityUtil.getCurrentMemberId();
        
        Organization organization = new Organization(request.getOrganizationName(), request.getDescription());
        organization.setRegId(currentUserId);
        organization.setChgId(currentUserId);
        
        organizationMapper.insertOrganization(organization);
        
        log.info("Successfully created organization with name: {}", request.getOrganizationName());
        return OrganizationResponse.from(organization);
    }

    public OrganizationResponse updateOrganization(Integer organizationId, UpdateOrganizationRequest request) {
        log.info("Updating organization with ID: {}", organizationId);
        
        Optional<Organization> existingOrganization = organizationMapper.selectOrganizationByOrganizationId(organizationId);
        if (existingOrganization.isEmpty()) {
            throw new IllegalArgumentException("Organization not found with ID: " + organizationId);
        }

        Organization organization = existingOrganization.get();
        
        // Check if new name already exists for different organization
        if (!organization.getOrganizationName().equals(request.getOrganizationName())) {
            Optional<Organization> orgWithSameName = organizationMapper.selectOrganizationByOrganizationName(request.getOrganizationName());
            if (orgWithSameName.isPresent()) {
                throw new IllegalArgumentException("Organization with name " + request.getOrganizationName() + " already exists");
            }
        }

        organization.setOrganizationName(request.getOrganizationName());
        organization.setDescription(request.getDescription());
        organization.setChgId(SecurityUtil.getCurrentMemberId());
        organization.setChgDt(LocalDateTime.now());
        
        organizationMapper.updateOrganization(organization);
        log.info("Successfully updated organization with ID: {}", organizationId);
        
        return OrganizationResponse.from(organization);
    }

    public void deleteOrganization(Integer organizationId) {
        log.info("Deleting organization with ID: {}", organizationId);
        
        if (organizationMapper.selectOrganizationByOrganizationId(organizationId).isEmpty()) {
            throw new IllegalArgumentException("Organization not found with ID: " + organizationId);
        }

        organizationMapper.deleteOrganizationByOrganizationId(organizationId);
        log.info("Successfully deleted organization with ID: {}", organizationId);
    }
    
    public OrganizationResponse getOrganizationByOrganizationId(Integer organizationId) {
        log.debug("Getting organization by ID: {}", organizationId);
        return findById(organizationId)
                .map(OrganizationResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + organizationId));
    }
    
    public OrganizationResponse getOrganizationByOrganizationName(String organizationName) {
        log.debug("Getting organization by name: {}", organizationName);
        return findByName(organizationName)
                .map(OrganizationResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with name: " + organizationName));
    }
    
    public List<OrganizationResponse> getAllOrganizations() {
        log.debug("Getting all organizations");
        return findAll().stream()
                .map(OrganizationResponse::from)
                .toList();
    }
}