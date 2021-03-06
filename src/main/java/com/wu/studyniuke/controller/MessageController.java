package com.wu.studyniuke.controller;

import com.wu.studyniuke.entity.Message;
import com.wu.studyniuke.entity.Page;
import com.wu.studyniuke.entity.User;
import com.wu.studyniuke.service.MessageService;
import com.wu.studyniuke.service.UserService;
import com.wu.studyniuke.util.CommunityUtil;
import com.wu.studyniuke.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author me
 * @create 2021-05-31-10:36
 */
@Controller
//@RequestMapping(path = "")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @RequestMapping(path="/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        List<Message> conversationList =
                messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());

        List<Map<String,Object>> conversations = new ArrayList<>();
        if(conversationList!=null){
            for (Message message : conversationList) {
                Map<String,Object> map = new HashMap<>();
                map.put("conversation",message);
                map.put("unreadCount",
                        messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                map.put("letterCount",
                        messageService.findLetterCounts(message.getConversationId()));
                int targetId = user.getId() == message.getFromId()?message.getToId(): message.getFromId();
                map.put("target",userService.findUserById(targetId));

                conversations.add(map);

            }
        }
        model.addAttribute("conversations",conversations);

        //????????????????????????
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        return "site/letter";
    }

    @RequestMapping(path="/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable(name = "conversationId")String conversationId,Page page,Model model){
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetterCounts(conversationId));
        page.setLimit(5);

        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());

        List<Map<String,Object>> letters = new ArrayList<>();
        if(letterList!=null){
            for (Message message : letterList) {
                Map<String,Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));

                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);

        model.addAttribute("target",getLetterTarget(conversationId));

        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "site/letter-detail";
    }

    private User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int smallId = Integer.parseInt(ids[0]);
        int bigId = Integer.parseInt(ids[1]);
        return hostHolder.getUser().getId()==smallId?userService.findUserById(bigId):userService.findUserById(smallId);
    }

//    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
//    public String sendLetter(Message message){
//        if(message==null){
//            throw new IllegalArgumentException("not null");
//        }
//        StringBuilder stringBuilder = new StringBuilder();
//        if(message.getFromId()<message.getToId()){
//            stringBuilder.append(message.getFromId());
//            stringBuilder.append("_");
//            stringBuilder.append(message.getToId());
//        }else{
//            stringBuilder.append(message.getToId());
//            stringBuilder.append("_");
//            stringBuilder.append(message.getFromId());
//        }
//        message.setCreateTime(new Date());
//        message.setConversationId(stringBuilder.toString());
//        message.setStatus(0);
//
//        messageService.addMessage(message);
//        return "redirect:/letter/list";
//    }

    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "?????????????????????!");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }


}
