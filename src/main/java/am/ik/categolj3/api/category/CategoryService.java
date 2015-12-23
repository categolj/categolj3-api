package am.ik.categolj3.api.category;

import java.util.List;

public interface CategoryService {
    List<List<String>> findAllOrderByNameAsc();
}
