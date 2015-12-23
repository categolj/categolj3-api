package am.ik.categolj3.api.category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "api")
public class CategoryRestController {
    @Autowired
    CategoryService categoryService;

    @RequestMapping(path = "categories", method = RequestMethod.GET)
    List<List<String>> list() {
        return categoryService.findAllOrderByNameAsc();
    }
}
