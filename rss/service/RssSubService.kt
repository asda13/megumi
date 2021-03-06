package com.erisu.cloud.megumi.rss.service

import com.erisu.cloud.megumi.command.Command
import com.erisu.cloud.megumi.command.CommandType
import com.erisu.cloud.megumi.pattern.Pattern
import com.erisu.cloud.megumi.plugin.pojo.Model
import com.erisu.cloud.megumi.rss.logic.RssLogic
import lombok.extern.slf4j.Slf4j
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import org.springframework.stereotype.Component
import javax.annotation.Resource

/**
 *@Description rss subscribe
 *@Author alice
 *@Date 2021/7/30 16:33
 **/
@Slf4j
@Component
@Model(name = "rss-subscribe")
class RssSubService {
    @Resource
    private lateinit var rssLogic: RssLogic

    @Command(commandType = CommandType.GROUP, value = "订阅up主", pattern = Pattern.PREFIX)
    @Throws(Exception::class)
    fun subscribeBilibili(sender: User, messageChain: MessageChain, subject: Contact): Message {
        val content = messageChain.contentToString().removePrefix("订阅up主").trim()
        val group = subject as Group
        val uid: String
        var nickname: String? = null
        if (content.contains(" ")) {
            val split = content.split(" ")
            uid = split[0]
            nickname = split[1]
        } else uid = content
        return rssLogic.subscribeBilibili(group.id.toString(), uid, nickname)
    }


    @Command(commandType = CommandType.GROUP, value = "订阅微博", pattern = Pattern.PREFIX)
    @Throws(Exception::class)
    fun subscribeWeibo(sender: User, messageChain: MessageChain, subject: Contact): Message {
        val content = messageChain.contentToString().removePrefix("订阅微博").trim()
        val group = subject as Group
        val uid: String
        var nickname: String? = null
        if (content.contains(" ")) {
            val split = content.split(" ")
            uid = split[0]
            nickname = split[1]
        } else uid = content
        return rssLogic.subscribeWeibo(group.id.toString(), uid, nickname)
    }

    @Command(commandType = CommandType.GROUP, value = "查看订阅(列表)?", pattern = Pattern.REGEX)
    @Throws(Exception::class)
    fun showSubscription(sender: User, messageChain: MessageChain, subject: Contact): Message {
        val group = subject as Group
        val subscription = rssLogic.getSubscription(group.id.toString())
        val subListInfo = subscription.joinToString("\n") { "${it.nickname}:\t${it.rssUrl}" }
        return PlainText("订阅列表\n${subListInfo}")
    }

    @Command(commandType = CommandType.GROUP, value = "取消订阅up主", pattern = Pattern.PREFIX)
    @Throws(Exception::class)
    fun unSubscribeBilibili(sender: User, messageChain: MessageChain, subject: Contact): Message {
        val uid = messageChain.contentToString().removePrefix("取消订阅up主").trim()
        val group = subject as Group
        return rssLogic.unSubscribeBilibili(group.id.toString(), uid)
    }


    @Command(commandType = CommandType.GROUP, value = "取消订阅微博", pattern = Pattern.PREFIX)
    @Throws(Exception::class)
    fun unSubscribeWeibo(sender: User, messageChain: MessageChain, subject: Contact): Message {
        val uid = messageChain.contentToString().removePrefix("取消订阅微博").trim()
        val group = subject as Group
        return rssLogic.unSubscribeWeibo(group.id.toString(), uid)
    }
}