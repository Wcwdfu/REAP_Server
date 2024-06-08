package Team_REAP.appserver.Controller;

import Team_REAP.appserver.Entity.User;
import Team_REAP.appserver.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

//    @PostMapping("")
//    public String create(@RequestParam String name) {
//        return userService.create(name);
//    }

    @GetMapping("")
    public User read(@RequestParam String id) {
        return userService.read(id);
    }

    @PutMapping("")
    public User update(@RequestParam String id, @RequestParam String name) {
        return userService.update(id, name);
    }

    @DeleteMapping("")
    public void delete(@RequestParam String id) {
        userService.delete(id);
    }
}
