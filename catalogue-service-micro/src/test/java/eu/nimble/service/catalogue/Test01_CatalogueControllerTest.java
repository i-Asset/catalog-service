package eu.nimble.service.catalogue;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.nimble.service.model.ubl.catalogue.CatalogueType;
import eu.nimble.service.model.ubl.commonaggregatecomponents.PartyType;
import eu.nimble.service.model.ubl.commonaggregatecomponents.ResourceType;
import eu.nimble.utility.persistence.GenericJPARepository;
import eu.nimble.utility.persistence.resource.ResourceValidationUtil;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("local_dev")
@RunWith(SpringJUnit4ClassRunner.class)
public class Test01_CatalogueControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private GenericJPARepository genericJpaRepository;
    @Autowired
    private ResourceValidationUtil resourceValidationUtil;

    private ObjectMapper mapper = new ObjectMapper();
    private static String createdCatalogueId;

    @Test
    public void test10_postJsonCatalogue() throws Exception {
        String catalogueJson = IOUtils.toString(Test01_CatalogueControllerTest.class.getResourceAsStream("/example_catalogue.json"));

        MockHttpServletRequestBuilder request = post("/catalogue/ubl")
                .contentType(MediaType.APPLICATION_JSON)
                .content(catalogueJson);
        MvcResult result = this.mockMvc.perform(request).andDo(print()).andExpect(status().isCreated()).andReturn();

        CatalogueType catalogue = mapper.readValue(result.getResponse().getContentAsString(), CatalogueType.class);
        createdCatalogueId = catalogue.getUUID();

        // check that resources have been managed properly
        List<ResourceType> allResources = genericJpaRepository.withEmf("ubldbEntityManagerFactory").getEntities(ResourceType.class);
        Set<Long> catalogueIds = resourceValidationUtil.extractAllHjidsExcludingPartyRelatedOnes(catalogue);

        Assert.assertEquals("Resource numbers and managed id sizes do not match", allResources.size(), catalogueIds.size());
        Set<Long> managedIds = new HashSet<>();
        for(ResourceType resource : allResources) {
            managedIds.add(resource.getEntityID());
        }
        Assert.assertTrue("Managed ids and catalogue ids do not match", managedIds.containsAll(catalogueIds) && catalogueIds.containsAll(managedIds));

        // check that only a single party instance is created
        List<PartyType> parties = genericJpaRepository.withEmf("ubldbEntityManagerFactory").getEntities(PartyType.class);
        Assert.assertEquals(1, parties.size());
    }

    @Test
    public void test11_postExistingCatalogue() throws Exception {
        String catalogueJson = IOUtils.toString(Test01_CatalogueControllerTest.class.getResourceAsStream("/example_catalogue.json"));

        MockHttpServletRequestBuilder request = post("/catalogue/ubl")
                .contentType(MediaType.APPLICATION_JSON)
                .content(catalogueJson);
        this.mockMvc.perform(request).andDo(print()).andExpect(status().isConflict()).andReturn();
    }

    @Test
    public void test2_getJsonCatalogue() throws Exception {
        MockHttpServletRequestBuilder request = get("/catalogue/ubl/" + createdCatalogueId);
        MvcResult result = this.mockMvc.perform(request).andDo(print()).andExpect(status().isOk()).andReturn();
        CatalogueType catalogue = mapper.readValue(result.getResponse().getContentAsString(), CatalogueType.class);
        Assert.assertEquals(createdCatalogueId, catalogue.getUUID());

        // check that only a single party instance is created
        List<PartyType> parties = genericJpaRepository.withEmf("ubldbEntityManagerFactory").getEntities(PartyType.class);
        Assert.assertEquals(1, parties.size());
    }

    @Test
    public void test31_updateJsonCatalogue() throws Exception {
        MockHttpServletRequestBuilder request = get("/catalogue/ubl/" + createdCatalogueId);
        MvcResult result = this.mockMvc.perform(request).andReturn();
        CatalogueType catalogue = mapper.readValue(result.getResponse().getContentAsString(), CatalogueType.class);

        // update party address
        catalogue.getCatalogueLine().get(0).getGoodsItem().getItem().setName("Updated product name");

        // get Json version of the updated catalogue
        String catalogueTypeAsString = mapper.writeValueAsString(catalogue);

        // make request
        request = put("/catalogue/ubl")
                .contentType(MediaType.APPLICATION_JSON)
                .content(catalogueTypeAsString);
        result = this.mockMvc.perform(request).andDo(print()).andExpect(status().isOk()).andReturn();
        catalogue = mapper.readValue(result.getResponse().getContentAsString(), CatalogueType.class);

        // check whether it is updated or not
        Assert.assertEquals("Updated product name", catalogue.getCatalogueLine().get(0).getGoodsItem().getItem().getName());

        // check that resources have been managed properly
        List<ResourceType> allResources = genericJpaRepository.withEmf("ubldbEntityManagerFactory").getEntities(ResourceType.class);
        Set<Long> catalogueIds = resourceValidationUtil.extractAllHjidsExcludingPartyRelatedOnes(catalogue);

        Assert.assertEquals("Resource numbers and managed id sizes do not match", allResources.size(), catalogueIds.size());
        Set<Long> managedIds = new HashSet<>();
        for(ResourceType resource : allResources) {
            managedIds.add(resource.getEntityID());
        }
        Assert.assertTrue("Managed ids and catalogue ids do not match", managedIds.containsAll(catalogueIds) && catalogueIds.containsAll(managedIds));

        // check that only a single party instance is created
        List<PartyType> parties = genericJpaRepository.withEmf("ubldbEntityManagerFactory").getEntities(PartyType.class);
        Assert.assertEquals(1, parties.size());
    }

    @Test
    public void test32_updateJsonCatalogue() throws Exception {
        MockHttpServletRequestBuilder request = get("/catalogue/ubl/" + createdCatalogueId);
        MvcResult result = this.mockMvc.perform(request).andReturn();
        CatalogueType catalogue = mapper.readValue(result.getResponse().getContentAsString(), CatalogueType.class);


        // get Json version of the updated catalogue
        String catalogueTypeAsString = mapper.writeValueAsString(catalogue);

        // make request
        request = put("/catalogue/ubl")
                .contentType(MediaType.APPLICATION_JSON)
                .content(catalogueTypeAsString);
        result = this.mockMvc.perform(request).andDo(print()).andExpect(status().isOk()).andReturn();
        catalogue = mapper.readValue(result.getResponse().getContentAsString(), CatalogueType.class);

        // check whether it is updated or not
        Assert.assertEquals("Updated product name", catalogue.getCatalogueLine().get(0).getGoodsItem().getItem().getName());

        // check that resources have been managed properly
        List<ResourceType> allResources = genericJpaRepository.withEmf("ubldbEntityManagerFactory").getEntities(ResourceType.class);
        Set<Long> catalogueIds = resourceValidationUtil.extractAllHjidsExcludingPartyRelatedOnes(catalogue);

        Assert.assertEquals("Resource numbers and managed id sizes do not match", allResources.size(), catalogueIds.size());
        Set<Long> managedIds = new HashSet<>();
        for(ResourceType resource : allResources) {
            managedIds.add(resource.getEntityID());
        }
        Assert.assertTrue("Managed ids and catalogue ids do not match", managedIds.containsAll(catalogueIds) && catalogueIds.containsAll(managedIds));

        // check that only a single party instance is created
        List<PartyType> parties = genericJpaRepository.withEmf("ubldbEntityManagerFactory").getEntities(PartyType.class);
        Assert.assertEquals(1, parties.size());
    }

    @Test
    public void test4_deleteCatalogue() throws Exception {
        MockHttpServletRequestBuilder request = delete("/catalogue/ubl/" + createdCatalogueId);
        this.mockMvc.perform(request).andDo(print()).andExpect(status().isOk()).andReturn();

        request = get("/catalogue/ubl/" + createdCatalogueId);
        this.mockMvc.perform(request).andDo(print()).andExpect(status().isNoContent()).andReturn();
    }

    @Test
    public void test5_getJsonNonExistingCatalogue() throws Exception {
        MockHttpServletRequestBuilder request = get("/catalogue/ubl/" + createdCatalogueId);
        this.mockMvc.perform(request).andDo(print()).andExpect(status().isNoContent()).andReturn();
    }
}
