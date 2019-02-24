package com.mrmodise.web.rest;

import com.mrmodise.TodoApp;

import com.mrmodise.domain.Activity;
import com.mrmodise.domain.Category;
import com.mrmodise.repository.ActivityRepository;
import com.mrmodise.service.ActivityService;
import com.mrmodise.web.rest.errors.ExceptionTranslator;
import com.mrmodise.service.dto.ActivityCriteria;
import com.mrmodise.service.ActivityQueryService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;


import static com.mrmodise.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ActivityResource REST controller.
 *
 * @see ActivityResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TodoApp.class)
public class ActivityResourceIntTest {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_DUE_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DUE_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final Boolean DEFAULT_COMPLETED = false;
    private static final Boolean UPDATED_COMPLETED = true;

    private static final String DEFAULT_CATEGORY = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY = "BBBBBBBBBB";

    private static final Boolean DEFAULT_IS_COLLAPSED = false;
    private static final Boolean UPDATED_IS_COLLAPSED = true;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityQueryService activityQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restActivityMockMvc;

    private Activity activity;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ActivityResource activityResource = new ActivityResource(activityService, activityQueryService);
        this.restActivityMockMvc = MockMvcBuilders.standaloneSetup(activityResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Activity createEntity(EntityManager em) {
        Activity activity = new Activity()
            .title(DEFAULT_TITLE)
            .description(DEFAULT_DESCRIPTION)
            .dueDate(DEFAULT_DUE_DATE)
            .completed(DEFAULT_COMPLETED)
            .category(DEFAULT_CATEGORY)
            .isCollapsed(DEFAULT_IS_COLLAPSED);
        // Add required entity
        Category category = CategoryResourceIntTest.createEntity(em);
        em.persist(category);
        em.flush();
        activity.setCategories(category);
        return activity;
    }

    @Before
    public void initTest() {
        activity = createEntity(em);
    }

    @Test
    @Transactional
    public void createActivity() throws Exception {
        int databaseSizeBeforeCreate = activityRepository.findAll().size();

        // Create the Activity
        restActivityMockMvc.perform(post("/api/activities")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(activity)))
            .andExpect(status().isCreated());

        // Validate the Activity in the database
        List<Activity> activityList = activityRepository.findAll();
        assertThat(activityList).hasSize(databaseSizeBeforeCreate + 1);
        Activity testActivity = activityList.get(activityList.size() - 1);
        assertThat(testActivity.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testActivity.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testActivity.getDueDate()).isEqualTo(DEFAULT_DUE_DATE);
        assertThat(testActivity.isCompleted()).isEqualTo(DEFAULT_COMPLETED);
        assertThat(testActivity.getCategory()).isEqualTo(DEFAULT_CATEGORY);
        assertThat(testActivity.isIsCollapsed()).isEqualTo(DEFAULT_IS_COLLAPSED);
    }

    @Test
    @Transactional
    public void createActivityWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = activityRepository.findAll().size();

        // Create the Activity with an existing ID
        activity.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restActivityMockMvc.perform(post("/api/activities")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(activity)))
            .andExpect(status().isBadRequest());

        // Validate the Activity in the database
        List<Activity> activityList = activityRepository.findAll();
        assertThat(activityList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = activityRepository.findAll().size();
        // set the field null
        activity.setTitle(null);

        // Create the Activity, which fails.

        restActivityMockMvc.perform(post("/api/activities")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(activity)))
            .andExpect(status().isBadRequest());

        List<Activity> activityList = activityRepository.findAll();
        assertThat(activityList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDescriptionIsRequired() throws Exception {
        int databaseSizeBeforeTest = activityRepository.findAll().size();
        // set the field null
        activity.setDescription(null);

        // Create the Activity, which fails.

        restActivityMockMvc.perform(post("/api/activities")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(activity)))
            .andExpect(status().isBadRequest());

        List<Activity> activityList = activityRepository.findAll();
        assertThat(activityList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDueDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = activityRepository.findAll().size();
        // set the field null
        activity.setDueDate(null);

        // Create the Activity, which fails.

        restActivityMockMvc.perform(post("/api/activities")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(activity)))
            .andExpect(status().isBadRequest());

        List<Activity> activityList = activityRepository.findAll();
        assertThat(activityList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCompletedIsRequired() throws Exception {
        int databaseSizeBeforeTest = activityRepository.findAll().size();
        // set the field null
        activity.setCompleted(null);

        // Create the Activity, which fails.

        restActivityMockMvc.perform(post("/api/activities")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(activity)))
            .andExpect(status().isBadRequest());

        List<Activity> activityList = activityRepository.findAll();
        assertThat(activityList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCategoryIsRequired() throws Exception {
        int databaseSizeBeforeTest = activityRepository.findAll().size();
        // set the field null
        activity.setCategory(null);

        // Create the Activity, which fails.

        restActivityMockMvc.perform(post("/api/activities")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(activity)))
            .andExpect(status().isBadRequest());

        List<Activity> activityList = activityRepository.findAll();
        assertThat(activityList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllActivities() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList
        restActivityMockMvc.perform(get("/api/activities?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(activity.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].dueDate").value(hasItem(DEFAULT_DUE_DATE.toString())))
            .andExpect(jsonPath("$.[*].completed").value(hasItem(DEFAULT_COMPLETED.booleanValue())))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY.toString())))
            .andExpect(jsonPath("$.[*].isCollapsed").value(hasItem(DEFAULT_IS_COLLAPSED.booleanValue())));
    }
    
    @Test
    @Transactional
    public void getActivity() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get the activity
        restActivityMockMvc.perform(get("/api/activities/{id}", activity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(activity.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.dueDate").value(DEFAULT_DUE_DATE.toString()))
            .andExpect(jsonPath("$.completed").value(DEFAULT_COMPLETED.booleanValue()))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY.toString()))
            .andExpect(jsonPath("$.isCollapsed").value(DEFAULT_IS_COLLAPSED.booleanValue()));
    }

    @Test
    @Transactional
    public void getAllActivitiesByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where title equals to DEFAULT_TITLE
        defaultActivityShouldBeFound("title.equals=" + DEFAULT_TITLE);

        // Get all the activityList where title equals to UPDATED_TITLE
        defaultActivityShouldNotBeFound("title.equals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    public void getAllActivitiesByTitleIsInShouldWork() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where title in DEFAULT_TITLE or UPDATED_TITLE
        defaultActivityShouldBeFound("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE);

        // Get all the activityList where title equals to UPDATED_TITLE
        defaultActivityShouldNotBeFound("title.in=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    public void getAllActivitiesByTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where title is not null
        defaultActivityShouldBeFound("title.specified=true");

        // Get all the activityList where title is null
        defaultActivityShouldNotBeFound("title.specified=false");
    }

    @Test
    @Transactional
    public void getAllActivitiesByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where description equals to DEFAULT_DESCRIPTION
        defaultActivityShouldBeFound("description.equals=" + DEFAULT_DESCRIPTION);

        // Get all the activityList where description equals to UPDATED_DESCRIPTION
        defaultActivityShouldNotBeFound("description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllActivitiesByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where description in DEFAULT_DESCRIPTION or UPDATED_DESCRIPTION
        defaultActivityShouldBeFound("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION);

        // Get all the activityList where description equals to UPDATED_DESCRIPTION
        defaultActivityShouldNotBeFound("description.in=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllActivitiesByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where description is not null
        defaultActivityShouldBeFound("description.specified=true");

        // Get all the activityList where description is null
        defaultActivityShouldNotBeFound("description.specified=false");
    }

    @Test
    @Transactional
    public void getAllActivitiesByDueDateIsEqualToSomething() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where dueDate equals to DEFAULT_DUE_DATE
        defaultActivityShouldBeFound("dueDate.equals=" + DEFAULT_DUE_DATE);

        // Get all the activityList where dueDate equals to UPDATED_DUE_DATE
        defaultActivityShouldNotBeFound("dueDate.equals=" + UPDATED_DUE_DATE);
    }

    @Test
    @Transactional
    public void getAllActivitiesByDueDateIsInShouldWork() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where dueDate in DEFAULT_DUE_DATE or UPDATED_DUE_DATE
        defaultActivityShouldBeFound("dueDate.in=" + DEFAULT_DUE_DATE + "," + UPDATED_DUE_DATE);

        // Get all the activityList where dueDate equals to UPDATED_DUE_DATE
        defaultActivityShouldNotBeFound("dueDate.in=" + UPDATED_DUE_DATE);
    }

    @Test
    @Transactional
    public void getAllActivitiesByDueDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where dueDate is not null
        defaultActivityShouldBeFound("dueDate.specified=true");

        // Get all the activityList where dueDate is null
        defaultActivityShouldNotBeFound("dueDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllActivitiesByDueDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where dueDate greater than or equals to DEFAULT_DUE_DATE
        defaultActivityShouldBeFound("dueDate.greaterOrEqualThan=" + DEFAULT_DUE_DATE);

        // Get all the activityList where dueDate greater than or equals to UPDATED_DUE_DATE
        defaultActivityShouldNotBeFound("dueDate.greaterOrEqualThan=" + UPDATED_DUE_DATE);
    }

    @Test
    @Transactional
    public void getAllActivitiesByDueDateIsLessThanSomething() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where dueDate less than or equals to DEFAULT_DUE_DATE
        defaultActivityShouldNotBeFound("dueDate.lessThan=" + DEFAULT_DUE_DATE);

        // Get all the activityList where dueDate less than or equals to UPDATED_DUE_DATE
        defaultActivityShouldBeFound("dueDate.lessThan=" + UPDATED_DUE_DATE);
    }


    @Test
    @Transactional
    public void getAllActivitiesByCompletedIsEqualToSomething() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where completed equals to DEFAULT_COMPLETED
        defaultActivityShouldBeFound("completed.equals=" + DEFAULT_COMPLETED);

        // Get all the activityList where completed equals to UPDATED_COMPLETED
        defaultActivityShouldNotBeFound("completed.equals=" + UPDATED_COMPLETED);
    }

    @Test
    @Transactional
    public void getAllActivitiesByCompletedIsInShouldWork() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where completed in DEFAULT_COMPLETED or UPDATED_COMPLETED
        defaultActivityShouldBeFound("completed.in=" + DEFAULT_COMPLETED + "," + UPDATED_COMPLETED);

        // Get all the activityList where completed equals to UPDATED_COMPLETED
        defaultActivityShouldNotBeFound("completed.in=" + UPDATED_COMPLETED);
    }

    @Test
    @Transactional
    public void getAllActivitiesByCompletedIsNullOrNotNull() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where completed is not null
        defaultActivityShouldBeFound("completed.specified=true");

        // Get all the activityList where completed is null
        defaultActivityShouldNotBeFound("completed.specified=false");
    }

    @Test
    @Transactional
    public void getAllActivitiesByCategoryIsEqualToSomething() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where category equals to DEFAULT_CATEGORY
        defaultActivityShouldBeFound("category.equals=" + DEFAULT_CATEGORY);

        // Get all the activityList where category equals to UPDATED_CATEGORY
        defaultActivityShouldNotBeFound("category.equals=" + UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    public void getAllActivitiesByCategoryIsInShouldWork() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where category in DEFAULT_CATEGORY or UPDATED_CATEGORY
        defaultActivityShouldBeFound("category.in=" + DEFAULT_CATEGORY + "," + UPDATED_CATEGORY);

        // Get all the activityList where category equals to UPDATED_CATEGORY
        defaultActivityShouldNotBeFound("category.in=" + UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    public void getAllActivitiesByCategoryIsNullOrNotNull() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where category is not null
        defaultActivityShouldBeFound("category.specified=true");

        // Get all the activityList where category is null
        defaultActivityShouldNotBeFound("category.specified=false");
    }

    @Test
    @Transactional
    public void getAllActivitiesByIsCollapsedIsEqualToSomething() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where isCollapsed equals to DEFAULT_IS_COLLAPSED
        defaultActivityShouldBeFound("isCollapsed.equals=" + DEFAULT_IS_COLLAPSED);

        // Get all the activityList where isCollapsed equals to UPDATED_IS_COLLAPSED
        defaultActivityShouldNotBeFound("isCollapsed.equals=" + UPDATED_IS_COLLAPSED);
    }

    @Test
    @Transactional
    public void getAllActivitiesByIsCollapsedIsInShouldWork() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where isCollapsed in DEFAULT_IS_COLLAPSED or UPDATED_IS_COLLAPSED
        defaultActivityShouldBeFound("isCollapsed.in=" + DEFAULT_IS_COLLAPSED + "," + UPDATED_IS_COLLAPSED);

        // Get all the activityList where isCollapsed equals to UPDATED_IS_COLLAPSED
        defaultActivityShouldNotBeFound("isCollapsed.in=" + UPDATED_IS_COLLAPSED);
    }

    @Test
    @Transactional
    public void getAllActivitiesByIsCollapsedIsNullOrNotNull() throws Exception {
        // Initialize the database
        activityRepository.saveAndFlush(activity);

        // Get all the activityList where isCollapsed is not null
        defaultActivityShouldBeFound("isCollapsed.specified=true");

        // Get all the activityList where isCollapsed is null
        defaultActivityShouldNotBeFound("isCollapsed.specified=false");
    }

    @Test
    @Transactional
    public void getAllActivitiesByCategoriesIsEqualToSomething() throws Exception {
        // Initialize the database
        Category categories = CategoryResourceIntTest.createEntity(em);
        em.persist(categories);
        em.flush();
        activity.setCategories(categories);
        activityRepository.saveAndFlush(activity);
        Long categoriesId = categories.getId();

        // Get all the activityList where categories equals to categoriesId
        defaultActivityShouldBeFound("categoriesId.equals=" + categoriesId);

        // Get all the activityList where categories equals to categoriesId + 1
        defaultActivityShouldNotBeFound("categoriesId.equals=" + (categoriesId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultActivityShouldBeFound(String filter) throws Exception {
        restActivityMockMvc.perform(get("/api/activities?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(activity.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].dueDate").value(hasItem(DEFAULT_DUE_DATE.toString())))
            .andExpect(jsonPath("$.[*].completed").value(hasItem(DEFAULT_COMPLETED.booleanValue())))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)))
            .andExpect(jsonPath("$.[*].isCollapsed").value(hasItem(DEFAULT_IS_COLLAPSED.booleanValue())));

        // Check, that the count call also returns 1
        restActivityMockMvc.perform(get("/api/activities/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultActivityShouldNotBeFound(String filter) throws Exception {
        restActivityMockMvc.perform(get("/api/activities?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restActivityMockMvc.perform(get("/api/activities/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingActivity() throws Exception {
        // Get the activity
        restActivityMockMvc.perform(get("/api/activities/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateActivity() throws Exception {
        // Initialize the database
        activityService.save(activity);

        int databaseSizeBeforeUpdate = activityRepository.findAll().size();

        // Update the activity
        Activity updatedActivity = activityRepository.findById(activity.getId()).get();
        // Disconnect from session so that the updates on updatedActivity are not directly saved in db
        em.detach(updatedActivity);
        updatedActivity
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .dueDate(UPDATED_DUE_DATE)
            .completed(UPDATED_COMPLETED)
            .category(UPDATED_CATEGORY)
            .isCollapsed(UPDATED_IS_COLLAPSED);

        restActivityMockMvc.perform(put("/api/activities")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedActivity)))
            .andExpect(status().isOk());

        // Validate the Activity in the database
        List<Activity> activityList = activityRepository.findAll();
        assertThat(activityList).hasSize(databaseSizeBeforeUpdate);
        Activity testActivity = activityList.get(activityList.size() - 1);
        assertThat(testActivity.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testActivity.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testActivity.getDueDate()).isEqualTo(UPDATED_DUE_DATE);
        assertThat(testActivity.isCompleted()).isEqualTo(UPDATED_COMPLETED);
        assertThat(testActivity.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testActivity.isIsCollapsed()).isEqualTo(UPDATED_IS_COLLAPSED);
    }

    @Test
    @Transactional
    public void updateNonExistingActivity() throws Exception {
        int databaseSizeBeforeUpdate = activityRepository.findAll().size();

        // Create the Activity

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restActivityMockMvc.perform(put("/api/activities")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(activity)))
            .andExpect(status().isBadRequest());

        // Validate the Activity in the database
        List<Activity> activityList = activityRepository.findAll();
        assertThat(activityList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteActivity() throws Exception {
        // Initialize the database
        activityService.save(activity);

        int databaseSizeBeforeDelete = activityRepository.findAll().size();

        // Delete the activity
        restActivityMockMvc.perform(delete("/api/activities/{id}", activity.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Activity> activityList = activityRepository.findAll();
        assertThat(activityList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Activity.class);
        Activity activity1 = new Activity();
        activity1.setId(1L);
        Activity activity2 = new Activity();
        activity2.setId(activity1.getId());
        assertThat(activity1).isEqualTo(activity2);
        activity2.setId(2L);
        assertThat(activity1).isNotEqualTo(activity2);
        activity1.setId(null);
        assertThat(activity1).isNotEqualTo(activity2);
    }
}
