package org.mifosplatform.integrationtests.common.office;

import com.google.gson.Gson;

public class OfficeHelper {

    public static class Builder {

        private String name;
        private String externalId;
        private Long id;
        private String nameDecorated;
        private int openingDate[] = new int[3];
        private String hierarchy;


        private Builder(final String name) {
            this.name = name;
        }

        public Builder externalId(final String externalId) {
            this.externalId = externalId;
            return this;
        }


        public Builder nameDecorated(final String nameDecorated) {
            this.nameDecorated = nameDecorated;
        }

        public Builder openingDate(final int[] openingDate) {
            this.openingDate = openingDate;
        }

        public Builder hierarchy(final String hierarchy) {
            this.hierarchy = hierarchy;
        }
        public OfficeHelper build() {
            return new OfficeHelper(this.name, this.externalId, this.nameDecorated, this.openingDate, this.hierarchy);
        }

    }



        private String name;
        private String externalId;
        private Long id;
        private String nameDecorated;


        OfficeHelper() {
            super();
        }

        private OfficeHelper(final String name, final String externalId, final String nameDecorated, final String hierarchy, final int[] openingDate) {
            super();
            this.name = name;
            this.externalId = externalId;
            this.nameDecorated = nameDecorated;
            this.hierarchy = hierarchy;
            this.openingDate = openingDate;
        }


    }

    public String toJSON() {
        return new Gson().toJson(this);
    }

    public static OfficeHelper fromJSON(final String jsonData) {
        return new Gson().fromJson(jsonData, OfficeHelper.class);
    }

    public String getName() {
        return this.name;
    }

    public Long getId() {
        return this.id;
    }

    public String getNameDecorated()
    { return this.nameDecorated; }

    public String getHierarchy()
    { return this.hierarchy; }


    public String getExternalId() {
        return this.externalId;
    }

    public int openingDate() {
        return this.openingDate;
    }

    public static Builder create(final String name) {
        return new Builder(name);
    }



}


