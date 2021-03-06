package com.qcadoo.view.internal.utils;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.utils.NumberGeneratorModelHelper;
import com.qcadoo.view.api.utils.NumberGeneratorService;

public class NumberGeneratorServiceTest {

    private static final String PLUGIN_IDENTIFIER = "somePlugin";

    private static final String MODEL_NAME = "model";

    private NumberGeneratorService numberGeneratorService;

    @Mock
    private NumberGeneratorModelHelper numberGeneratorModelHelper;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);

        numberGeneratorService = new NumberGeneratorService();
        ReflectionTestUtils.setField(numberGeneratorService, "numberGeneratorModelHelper", numberGeneratorModelHelper);
    }

    private void stubExistingNumbers(final Iterable<String> numbers) {
        final Collection<Entity> projectionEntities = mockProjectionEntities(numbers);
        given(numberGeneratorModelHelper.getNumbersProjection(anyString(), anyString(), anyString())).willAnswer(
                new Answer<Collection<Entity>>() {

                    @Override
                    public Collection<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                        return Lists.newLinkedList(projectionEntities);
                    }
                });
    }

    private Collection<Entity> mockProjectionEntities(final Iterable<String> numbers) {
        List<Entity> projectionEntities = Lists.newArrayList();
        for (String number : numbers) {
            projectionEntities.add(mockProjectionEntity(number));
        }
        return projectionEntities;
    }

    private Entity mockProjectionEntity(final String number) {
        Entity projectionEntity = mock(Entity.class);
        given(projectionEntity.getStringField(NumberGeneratorModelHelper.NUM_PROJECTION_ALIAS)).willReturn(number);
        given(projectionEntity.getField(NumberGeneratorModelHelper.NUM_PROJECTION_ALIAS)).willReturn(number);
        return projectionEntity;
    }

    private String performGenerate() {
        return numberGeneratorService.generateNumber(PLUGIN_IDENTIFIER, MODEL_NAME, NumberGeneratorService.DEFAULT_NUM_OF_DIGITS);
    }

    @Test
    public final void shouldReturnOneIfThereIsNoExistingValues() {
        // given
        stubExistingNumbers(Lists.<String> newArrayList());

        // when
        String generated = performGenerate();

        // then
        Assert.assertEquals("000001", generated);
    }

    @Test
    public final void shouldReturnNextAvailableNumber() {
        // given
        stubExistingNumbers(Lists.newArrayList("1", "0002"));

        // when
        String generated = performGenerate();

        // then
        Assert.assertEquals("000003", generated);
    }

    @Test
    public final void shouldReturnNextAvailableNumberGreaterThanMaxOne() {
        // given
        stubExistingNumbers(Lists.newArrayList("1", "seventeen", "15", "2", "16", "7", "test"));

        // when
        String generated = performGenerate();

        // then
        Assert.assertEquals("000017", generated);
    }

    @Test
    public final void shouldIgnoreNonNumericValues() {
        // given
        stubExistingNumbers(Lists.newArrayList("1", "hello", "1st", "16"));

        // when
        String generated = performGenerate();

        // then
        Assert.assertEquals("000017", generated);
    }

    @Test
    public final void shouldReturnNextAvailableNumberGreaterThanMaxOneEvenIfThereIsSimilarValues() {
        // given
        stubExistingNumbers(Lists.newArrayList("0002", "1", "000001"));

        // when
        String generated = performGenerate();

        // then
        Assert.assertEquals("000003", generated);
    }
}
