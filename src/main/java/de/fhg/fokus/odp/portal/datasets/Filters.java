/**
 * Copyright (c) 2012, 2013 Fraunhofer Institute FOKUS
 *
 * This file is part of Open Data Platform.
 *
 * Open Data Platform is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Open Data Plaform is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with Open Data Platform.  If not, see <http://www.gnu.org/licenses/agpl-3.0>.
 */

/**
 * 
 */
package de.fhg.fokus.odp.portal.datasets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.portlet.ActionResponse;

import de.fhg.fokus.odp.registry.model.Category;
import de.fhg.fokus.odp.registry.model.MetadataEnumType;
import de.fhg.fokus.odp.registry.queries.Query;
import de.fhg.fokus.odp.registry.queries.QueryFacet;
import de.fhg.fokus.odp.registry.queries.QueryFacetItem;
import de.fhg.fokus.odp.registry.queries.QueryResult;

/**
 * @author sim
 * 
 */
@ManagedBean
@ViewScoped
public class Filters implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -6771223667157622098L;

    private QueryFacet categories;
    private QueryFacet types;
    private QueryFacet formats;
    private QueryFacet tags;
    private QueryFacet licences;
    private QueryFacet openess;

    private List<String> selectedTypes;

    private List<String> selectedCategories;

    private List<String> selectedTags;

    private List<String> selectedFormats;

    private List<String> selectedLicences;

    private String selectedOpeness;

    @ManagedProperty("#{currentQuery}")
    private CurrentQuery currentQuery;

    @ManagedProperty("#{registryClient}")
    private RegistryClient registryClient;

    @PostConstruct
    public void init() {

        QueryResult<?> lastResult = currentQuery.getLastResult();

        selectedTags = currentQuery.getQuery().getTags();
        selectedCategories = currentQuery.getQuery().getCategories();
        selectedTypes = new ArrayList<String>();
        for (MetadataEnumType type : currentQuery.getQuery().getTypes()) {
            selectedTypes.add(type.toField());
        }
        selectedFormats = currentQuery.getQuery().getFormats();
        selectedLicences = currentQuery.getQuery().getLicences();
        Boolean isopen = currentQuery.getQuery().getIsOpen();
        if (isopen == null) {
            selectedOpeness = "0";
        } else if (isopen) {
            selectedOpeness = "true";
        } else {
            selectedOpeness = "false";
        }

        if (lastResult != null) {
            categories = lastResult.getFacets().get("groups");
            if (categories != null && !categories.getItems().isEmpty()) {
                Collections.sort(categories.getItems(), new FilterCountComparator());
                for (Category category : registryClient.getCategories()) {
                    Iterator<QueryFacetItem> it = categories.getItems().iterator();
                    while (it.hasNext()) {
                        QueryFacetItem item = it.next();
                        if (item.getName().equals(category.getName()) && category.getType().equals("subgroup")) {
                            it.remove();
                        }
                    }
                }
            }

            openess = lastResult.getFacets().get("isopen");
            if (openess != null) {

            }

            types = lastResult.getFacets().get("type");
            if (types != null) {
                Collections.sort(types.getItems(), new FilterCountComparator());
            }

            formats = lastResult.getFacets().get("res_format");
            if (formats != null) {
                Collections.sort(formats.getItems(), new FilterCountComparator());

                if (formats.getItems().size() > 4) {
                    List<QueryFacetItem> tmpList = new ArrayList<QueryFacetItem>(formats.getItems().subList(0, 5));
                    formats.getItems().clear();
                    formats.getItems().addAll(tmpList);
                }
            }

            tags = lastResult.getFacets().get("tags");
            if (tags != null) {
                Collections.sort(tags.getItems(), new FilterCountComparator());

                if (tags.getItems().size() > 4) {
                    List<QueryFacetItem> tmpList = new ArrayList<QueryFacetItem>(tags.getItems().subList(0, 5));
                    tags.getItems().clear();
                    tags.getItems().addAll(tmpList);
                }
            }

            licences = lastResult.getFacets().get("license_id");
            if (licences != null) {
                Collections.sort(licences.getItems(), new FilterCountComparator());
            }
        }
    }

    public void setSelectedTypes(List<String> selectedTypes) {
        this.selectedTypes = selectedTypes;
    }

    public List<String> getSelectedTypes() {
        return selectedTypes;
    }

    public void openessValueChanged(ValueChangeEvent event) {

    }

    @SuppressWarnings("unchecked")
    public void typeValueChanged(ValueChangeEvent event) {
        Query query = currentQuery.getQuery();

        List<String> oldItems = new ArrayList<String>();
        oldItems.addAll((List<String>) event.getOldValue());

        List<String> newItems = new ArrayList<String>();
        newItems.addAll((List<String>) event.getNewValue());

        query.getTypes().clear();
        for (String type : newItems) {
            query.getTypes().add(MetadataEnumType.fromField(type));
        }

        if (oldItems.size() < newItems.size()) {
            newItems.removeAll(oldItems);
        } else {
            oldItems.removeAll(newItems);
        }
        // currentQuery.updateQuery(query);
        // init();
    }

    public void setSelectedLicences(List<String> selectedLicences) {
        this.selectedLicences = selectedLicences;
    }

    public List<String> getSelectedLicences() {
        return selectedLicences;
    }

    @SuppressWarnings("unchecked")
    public void licenceValueChanged(ValueChangeEvent event) {
        Query query = currentQuery.getQuery();

        List<String> oldItems = new ArrayList<String>();
        oldItems.addAll((List<String>) event.getOldValue());

        List<String> newItems = new ArrayList<String>();
        newItems.addAll((List<String>) event.getNewValue());

        query.getLicences().clear();
        for (String licence : newItems) {
            query.getLicences().add(licence);
        }

        if (oldItems.size() < newItems.size()) {
            newItems.removeAll(oldItems);
        } else {
            oldItems.removeAll(newItems);
        }
        // currentQuery.updateQuery(query);
        // init();
    }

    public void setSelectedTags(List<String> selectedTags) {
        this.selectedTags = selectedTags;
    }

    public List<String> getSelectedTags() {
        return selectedTags;
    }

    @SuppressWarnings("unchecked")
    public void tagValueChanged(ValueChangeEvent event) {
        Query query = currentQuery.getQuery();

        List<String> oldItems = new ArrayList<String>();
        oldItems.addAll((List<String>) event.getOldValue());

        List<String> newItems = new ArrayList<String>();
        newItems.addAll((List<String>) event.getNewValue());

        query.getTags().clear();
        for (String tag : newItems) {
            query.getTags().add(tag);
        }

        if (oldItems.size() < newItems.size()) {
            newItems.removeAll(oldItems);
        } else {
            oldItems.removeAll(newItems);
        }
        // currentQuery.updateQuery(query);
        // init();
    }

    public void setSelectedCategories(List<String> selectedCategories) {
        this.selectedCategories = selectedCategories;
    }

    public List<String> getSelectedCategories() {
        return selectedCategories;
    }

    @SuppressWarnings("unchecked")
    public void categoryValueChanged(ValueChangeEvent event) {
        Query query = currentQuery.getQuery();

        List<String> oldItems = new ArrayList<String>();
        oldItems.addAll((List<String>) event.getOldValue());

        List<String> newItems = new ArrayList<String>();
        newItems.addAll((List<String>) event.getNewValue());

        query.getCategories().clear();
        for (String category : newItems) {
            query.getCategories().add(category);
        }

        if (oldItems.size() < newItems.size()) {
            newItems.removeAll(oldItems);
        } else {
            oldItems.removeAll(newItems);
        }
        // currentQuery.updateQuery(query);
        // init();
    }

    public void setSelectedFormats(List<String> selectedFormats) {
        this.selectedFormats = selectedFormats;
    }

    public List<String> getSelectedFormats() {
        return selectedFormats;
    }

    @SuppressWarnings("unchecked")
    public void formatValueChanged(ValueChangeEvent event) {
        Query query = currentQuery.getQuery();

        List<String> oldItems = new ArrayList<String>();
        oldItems.addAll((List<String>) event.getOldValue());

        List<String> newItems = new ArrayList<String>();
        newItems.addAll((List<String>) event.getNewValue());

        query.getFormats().clear();
        for (String format : newItems) {
            query.getFormats().add(format);
        }

        if (oldItems.size() < newItems.size()) {
            newItems.removeAll(oldItems);
        } else {
            oldItems.removeAll(newItems);
        }
        // currentQuery.updateQuery(query);
        // init();
    }

    /**
     * @return the categories
     */
    public List<QueryFacetItem> getCategories() {
        return categories == null ? new ArrayList<QueryFacetItem>() : categories.getItems();
    }

    /**
     * @return the categories
     */
    public List<QueryFacetItem> getTypes() {
        return types == null ? new ArrayList<QueryFacetItem>() : types.getItems();
    }

    public List<QueryFacetItem> getFormats() {
        return formats == null ? new ArrayList<QueryFacetItem>() : formats.getItems();
    }

    public List<QueryFacetItem> getTags() {
        return tags == null ? new ArrayList<QueryFacetItem>() : tags.getItems();
    }

    public List<QueryFacetItem> getLicences() {
        return licences == null ? new ArrayList<QueryFacetItem>() : licences.getItems();
    }

    public List<QueryFacetItem> getOpeness() {
        return openess == null ? new ArrayList<QueryFacetItem>() : openess.getItems();
    }

    /**
     * @param queryResult
     *            the queryResult to set
     */
    public void setCurrentQuery(CurrentQuery currentQuery) {
        this.currentQuery = currentQuery;
    }

    public void setRegistryClient(RegistryClient registryClient) {
        this.registryClient = registryClient;
    }

    public void filter(ActionEvent event) {

        ActionResponse response = (ActionResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.removePublicRenderParameter("searchcategory");

        Query query = currentQuery.getQuery();

        if (selectedTypes != null && !selectedTypes.isEmpty()) {
            query.getTypes().clear();
            for (String type : selectedTypes) {
                query.getTypes().add(MetadataEnumType.fromField(type));
            }
        }

        if ("0".equals(selectedOpeness)) {
            query.setIsOpen(null);
        } else if ("true".equals(selectedOpeness)) {
            query.setIsOpen(true);
        } else if ("false".equals(selectedOpeness)) {
            query.setIsOpen(false);
        }

        query.getCategories().clear();
        query.getCategories().addAll(selectedCategories);

        query.getTags().clear();
        query.getTags().addAll(selectedTags);

        query.getFormats().clear();
        query.getFormats().addAll(selectedFormats);

        query.getLicences().clear();
        query.getLicences().addAll(selectedLicences);

        currentQuery.updateQuery(query);
        init();
    }

    public void setFilterType(String type) {
        Query query = currentQuery.getQuery();
        query.getTypes().add(MetadataEnumType.fromField(type));

        // currentQuery.updateQuery(query);
        // init();
    }

    /**
     * @return the selectedOpeness
     */
    public String getSelectedOpeness() {
        return selectedOpeness;
    }

    /**
     * @param selectedOpeness
     *            the selectedOpeness to set
     */
    public void setSelectedOpeness(String selectedOpeness) {
        this.selectedOpeness = selectedOpeness;
    }

}
