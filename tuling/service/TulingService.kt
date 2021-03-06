package com.erisu.cloud.megumi.tuling.service

import com.erisu.cloud.megumi.command.Command
import com.erisu.cloud.megumi.command.CommandType
import com.erisu.cloud.megumi.message.MessageUtil
import com.erisu.cloud.megumi.pattern.Pattern
import com.erisu.cloud.megumi.plugin.pojo.Model
import com.erisu.cloud.megumi.tuling.logic.BaiduNlpLogic
import com.erisu.cloud.megumi.tuling.logic.TulingLogic
import kotlinx.serialization.ExperimentalSerializationApi
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.MiraiExperimentalApi
import org.springframework.stereotype.Component
import javax.annotation.Resource
import kotlin.random.Random

/**
 *@Description tuling测试
 *@Author alice
 *@Date 2021/7/23 15:42
 **/
@Component
@Model(name = "tuling")
@OptIn(ExperimentalSerializationApi::class)
class TulingService {
    companion object {
        var probabilities: MutableMap<Long, Int> = mutableMapOf()
    }

    @Resource
    private lateinit var tulingLogic: TulingLogic

//    @Resource
//    private lateinit var baiduNlpLogic: BaiduNlpLogic

    @Resource
    private lateinit var messageUtil: MessageUtil

    @Command(commandType = CommandType.GROUP,
        pattern = Pattern.CHECK,
        uuid = "8c7f68756891484aa2a7ee25e04f4c4a")
    fun onlineAnswering(sender: User, messageChain: MessageChain, subject: Contact): Message? {
        val group = subject as Group
        if (!probabilities.containsKey(group.id)) {
            probabilities[group.id] = 1
        }
        val contentToString = messageChain.contentToString()
        if (contentToString.startsWith("调整AI概率") || contentToString.startsWith("调整百度AI")) {
            return null
        }
        val probability = Random.nextInt(100)

        if (probability < probabilities[group.id]!!) {
            val answer = tulingLogic.onlineAnswering(contentToString)
            messageUtil.sendAsyncMessage(subject as Group?, answer, 2)
        }
        return null
    }

    @Command(commandType = CommandType.GROUP, value = "调整AI概率", pattern = Pattern.PREFIX)
    @Throws(Exception::class)
    fun changeProbability(sender: User, messageChain: MessageChain, subject: Contact): Message? {
        val member = sender as Member
        val group = subject as Group
        if (member.permission.level == 0 && member.id != 1269732086L) {
            return null
        }
        val probability = messageChain.contentToString().removePrefix("调整AI概率").trim().toInt()
        probabilities[group.id] = if (probability > 100) 100 else probability
        return PlainText("当前AI概率为$probability")
    }

//    @Command(commandType = CommandType.GROUP, value = "", pattern = Pattern.CHECK, probaility = 0.005)
//    @Throws(Exception::class)
//    suspend fun checkEmotion(sender: User, messageChain: MessageChain, subject: Contact): Message? {
//        return baiduNlpLogic.emotionRecognition(subject as Group, messageChain.contentToString())
//    }


}