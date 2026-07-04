package org.flux.store.tests.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.flux.store.api.v1.State;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CatalogState implements State {

    private List<CatalogItem> items = new ArrayList<>();

    @Override
    public CatalogState clone() {
        try {
            CatalogState copy = (CatalogState) super.clone();
            copy.items = new ArrayList<>();
            for (CatalogItem item : this.items) {
                copy.items.add(new CatalogItem(item.getId(), item.getName(), item.getPrice()));
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
