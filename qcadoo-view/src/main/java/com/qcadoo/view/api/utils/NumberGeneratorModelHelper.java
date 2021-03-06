package com.qcadoo.view.api.utils;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class NumberGeneratorModelHelper {

    public static final String NUM_PROJECTION_ALIAS = "numProjection";

    private static final String GET_NUMBERS_QUERY_TEMPLATE = "select distinct trim(LEADING '0' from %s) as "
            + NUM_PROJECTION_ALIAS + " from #%s_%s order by " + NUM_PROJECTION_ALIAS + " desc";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    /**
     * Returns a list of projection entities containing NUM_PROJECTION_ALIAS field with numberFieldName values with trimmed out
     * leading zeros. List is sorted descendant by numberFieldName.
     * 
     * @param pluginIdentifier
     *            identifier of the plugin
     * @param modelName
     *            name of the model
     * @param numberFieldName
     *            name of the field for which number will be generated
     * @return a list of projection entities containing NUM_PROJECTION_ALIAS field with numberFieldName values with trimmed out
     *         leading zeros. List is sorted descendant by numberFieldName.
     */
    public Collection<Entity> getNumbersProjection(final String pluginIdentifier, final String modelName,
            final String numberFieldName) {
        DataDefinition dd = dataDefinitionService.get(pluginIdentifier, modelName);
        String hqlQuery = String.format(GET_NUMBERS_QUERY_TEMPLATE, numberFieldName, pluginIdentifier, modelName);
        return dd.find(hqlQuery).list().getEntities();
    }

}
