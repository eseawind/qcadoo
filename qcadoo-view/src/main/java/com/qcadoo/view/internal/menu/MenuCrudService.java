package com.qcadoo.view.internal.menu;

import static com.qcadoo.model.api.search.SearchRestrictions.belongsTo;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.view.constants.MenuCategoryFields;
import com.qcadoo.view.constants.MenuItemFields;
import com.qcadoo.view.constants.QcadooViewConstants;
import com.qcadoo.view.constants.ViewFields;
import com.qcadoo.view.internal.menu.definitions.MenuCategoryDefinition;
import com.qcadoo.view.internal.menu.definitions.MenuItemDefinition;

@Service
public class MenuCrudService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Entity createEntity(final String modelName) {
        return getDataDefinition(modelName).create();
    }

    public void save(final Entity entity) {
        entity.getDataDefinition().save(entity);
    }

    public void delete(final Entity entity) {
        entity.getDataDefinition().delete(entity.getId());
    }

    public List<Entity> getSortedMenuCategories() {
        return getDataDefinition(QcadooViewConstants.MODEL_CATEGORY).find()
                .addOrder(SearchOrders.asc(MenuCategoryFields.SUCCESSION)).list().getEntities();
    }

    public Iterable<Entity> getSortedMenuCategoryItems(final Entity menuCategory) {
        return getDataDefinition(QcadooViewConstants.MODEL_ITEM).find().add(belongsTo(MenuItemFields.CATEGORY, menuCategory))
                .addOrder(SearchOrders.asc(MenuItemFields.SUCCESSION)).list().getEntities();
    }

    public int getTotalNumberOfCategories() {
        return getDataDefinition(QcadooViewConstants.MODEL_CATEGORY).find().list().getTotalNumberOfEntities() + 1;
    }

    public Entity getCategory(final MenuCategoryDefinition menuCategoryDefinition) {
        return getDataDefinition(QcadooViewConstants.MODEL_CATEGORY).find()
                .add(eq(MenuCategoryFields.NAME, menuCategoryDefinition.getName()))
                .add(eq(MenuCategoryFields.PLUGIN_IDENTIFIER, menuCategoryDefinition.getPluginIdentifier())).setMaxResults(1)
                .uniqueResult();
    }

    public Entity getCategory(final MenuItemDefinition itemDefinition) {
        return getDataDefinition(QcadooViewConstants.MODEL_CATEGORY).find()
                .add(eq(MenuCategoryFields.NAME, itemDefinition.getCategoryName())).setMaxResults(1).uniqueResult();
    }

    public Entity getItem(final MenuItemDefinition itemDefinition) {
        return getDataDefinition(QcadooViewConstants.MODEL_ITEM).find().add(eq(MenuItemFields.NAME, itemDefinition.getName()))
                .add(eq(MenuItemFields.PLUGIN_IDENTIFIER, itemDefinition.getPluginIdentifier())).setMaxResults(1).uniqueResult();
    }

    public Entity getView(final MenuItemDefinition itemDefinition) {
        return getDataDefinition(QcadooViewConstants.MODEL_VIEW).find().add(eq(ViewFields.NAME, itemDefinition.getViewName()))
                .add(eq(ViewFields.PLUGIN_IDENTIFIER, itemDefinition.getViewPluginIdentifier())).setMaxResults(1).uniqueResult();
    }

    private DataDefinition getDataDefinition(final String entityName) {
        return dataDefinitionService.get(QcadooViewConstants.PLUGIN_IDENTIFIER, entityName);
    }

}
