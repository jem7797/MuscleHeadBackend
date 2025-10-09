package com.MuscleHead.MuscleHead;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;






@RestController
@RequestMapping("UserControllerAPI/Mk1")
public class UserController {
    private final List<User> users = new ArrayList<>();


    @PostMapping("/create")
    public String createUser(@RequestBody User user) {
        users.add(user);      
        return "User created: " + user.getId();

    }


    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Integer id){
            boolean removed = users.removeIf(u -> u.getId().equals(id));
            return removed ? "User successfully removed" : "User not found";
    }


    @PutMapping("/{id}")
    public String putUser(@PathVariable Integer id, @RequestBody User updatedUser) {
                for(User u : users){
                    if(u.getId().equals(id)){
                        u.setName(updatedUser.getName());
                        u.setEmail(updatedUser.getEmail());
                        u.setUsername(updatedUser.getUsername());
                        
            
                        return "User " + updatedUser.getUsername() + " has been updated";
                    }
                }



        return "User not found";
    }

    @GetMapping("/get/{id}")
    public User getUserById(@PathVariable Integer id) {
       return users.stream()
       .filter(u -> u.getId().equals(id))
       .findFirst()
       .orElse(null);
    }
    



@GetMapping("/getUsers")
public List<User> getUsers() {
   return users;
}

}
