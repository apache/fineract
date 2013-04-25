package org.mifosplatform.portfolio.pagination;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class Page<E> {

    private int pageNumber;
    private int pagesAvailable;
    private List<E> pageItems = new ArrayList<E>();

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setPagesAvailable(int pagesAvailable) {
        this.pagesAvailable = pagesAvailable;
    }

    public void setPageItems(List<E> pageItems) {
        this.pageItems = pageItems;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPagesAvailable() {
        return pagesAvailable;
    }

    public List<E> getPageItems() {
        return pageItems;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
