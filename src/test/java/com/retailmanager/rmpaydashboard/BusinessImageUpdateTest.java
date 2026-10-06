package com.retailmanager.rmpaydashboard;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;
import com.retailmanager.rmpaydashboard.models.Address;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.User;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.services.DTO.AddressDTO;
import com.retailmanager.rmpaydashboard.services.DTO.BusinessDTO;
import com.retailmanager.rmpaydashboard.services.services.BusinessService.BusinessService;
import com.retailmanager.rmpaydashboard.services.services.FileServices.IFileService;

class BusinessImageUpdateTest {
    @ParameterizedTest
    @CsvSource(value = {"0,30166", "NULL,30166", "42,30166", "42,42", "42,NULL", "42,0", "0,NULL", "0,0"}, nullValues = "NULL")
    void updatesImagesWithoutDeletingLegacyZeroIds(Long oldId, Long requestedId) {
        BusinessService service = new BusinessService();
        BusinessRepository repository = mock(BusinessRepository.class);
        IFileService files = mock(IFileService.class);
        ModelMapper mapper = mock(ModelMapper.class);
        ReflectionTestUtils.setField(service, "serviceDBBusiness", repository);
        ReflectionTestUtils.setField(service, "fileService", files);
        ReflectionTestUtils.setField(service, "mapper", mapper);

        Business business = new Business();
        business.setMerchantId("merchant");
        business.setAddress(new Address());
        business.setUser(new User());
        business.setLogo(0L);
        business.setLogoAth(oldId);
        BusinessDTO request = new BusinessDTO();
        request.setMerchantId("merchant");
        request.setAddress(new AddressDTO());
        request.setLogo(0L);
        request.setLogoAth(requestedId);
        when(repository.findById(40029L)).thenReturn(Optional.of(business));
        when(repository.save(business)).thenReturn(business);
        when(mapper.map(business, BusinessDTO.class)).thenReturn(request);

        assertEquals(201, service.update(40029L, request).getStatusCode().value());
        Long expected = Long.valueOf(0L).equals(requestedId) ? null : requestedId;
        assertEquals(expected, business.getLogoAth());
        assertNull(business.getLogo());
        verify(repository).save(business);
        if (oldId != null && oldId != 0L && !oldId.equals(expected)) {
            verify(files).deleteImage(oldId);
        }
        verifyNoMoreInteractions(files);
    }
}
