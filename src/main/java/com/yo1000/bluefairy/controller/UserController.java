package com.yo1000.bluefairy.controller;

import com.yo1000.bluefairy.model.entity.User;
import com.yo1000.bluefairy.model.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;

/**
 * Created by yoichi.kikuchi on 15/02/19.
 */
@Controller
@RequestMapping("user")
public class UserController {
    @Resource
    private UserService userService;

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Users");
        model.addAttribute("users", this.getUserService().getUsersAll());

        return "user/items";
    }

    @RequestMapping("all")
    public String all(Model model) {
        model.addAttribute("title", "All users");
        model.addAttribute("users", this.getUserService().getUsersAll());

        return "user/items";
    }

    @RequestMapping("{id:(?!^(?:all|register|update)$)^.+$}")
    public String id(@PathVariable String id, Model model) {
        User user = this.getUserService().getUserById(id);

        model.addAttribute("title", "User " + user.getUsername());
        model.addAttribute("user", user);

        return "user/item";
    }

    @RequestMapping(value = "register", method = RequestMethod.GET)
    public String register(Model model) {
        model.addAttribute("title", "User registration");

        return "user/register";
    }

    @RequestMapping(value = "register", method = RequestMethod.POST)
    public String register(@RequestParam String username, @RequestParam String password,
                           @RequestParam String role, @RequestParam String fullname) {
        this.getUserService().registerUser(username, password, role, fullname);
        User user = this.getUserService().getUserByUsername(username);

        return "redirect:/user/" + user.getId();
    }

    protected UserService getUserService() {
        return userService;
    }
}
