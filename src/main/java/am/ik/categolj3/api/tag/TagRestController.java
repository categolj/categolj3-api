package am.ik.categolj3.api.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "api")
public class TagRestController {
    @Autowired
    TagService tagService;

    @RequestMapping(path = "tags", method = RequestMethod.GET)
    List<String> list() {
        return tagService.findAllOrderByNameAsc();
    }
}
