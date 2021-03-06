package com.erisu.cloud.megumi.setu.logic

import cn.hutool.core.img.ImgUtil
import cn.hutool.core.lang.UUID
import cn.hutool.http.HttpUtil
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.erisu.cloud.megumi.setu.pojo.SetuRequest
import com.erisu.cloud.megumi.setu.pojo.SetuResponse
import com.erisu.cloud.megumi.util.FileUtil
import com.erisu.cloud.megumi.util.StreamMessageUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.awt.Color
import java.awt.Font
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.pathString

/**
 *@Description setu logic
 *@Author alice
 *@Date 2021/7/7 8:46
 **/
@MiraiExperimentalApi
@Component
class SetuLogic {
    @Value("\${qq.username}")
    private val username: Long = 0

    suspend fun getRollSetu(tag: String, num: Int, isR18: Int, group: Group): Message? {
        val orTag = tag.split("|")
        val setuRequest: SetuRequest = if (tag.trim() == "") {
            SetuRequest(
                dateAfter = null,
                dateBefore = null,
                uid = null,
                tag = null,
                keyword = null,
                num = num,
                r18 = isR18
            )
        } else {
            SetuRequest(
                dateAfter = null,
                dateBefore = null,
                uid = null,
                tag = arrayOf(orTag),
                keyword = null,
                num = num, r18 = isR18
            )
        }
        group.sendMessage(PlainText("setu正在下载中，请稍等~"))
        val responseJson =
            HttpUtil.post("https://api.lolicon.app/setu/v2", JSON.toJSONString(setuRequest), 2000)
        val setuResponse = JSONObject.parseObject(responseJson, SetuResponse::class.java)
        if (setuResponse.error == "" && !setuResponse.data.isNullOrEmpty()) {
//            val imageList: MutableList<Image> = mutableListOf()
            if (isR18 == 0) {
                setuResponse.data.forEach {
                    val response = FileUtil.downloadHttpUrl(it.urls.original!!, "cache", null, null) ?: return null
//                    if (path != null) imageList.add(StreamMessageUtil.generateImage(group, path.toFile(), true))
                    //单条发送
                    val text = "pid：${it.pid}\n标题：${it.title}\n作者：${it.author}\n原地址：${it.urls.original}"
                    if (response.code != 200) {
                        group.sendMessage(
                            forwardSetuMessage(
                                PlainText(text),
                                StreamMessageUtil.generateImage(group, response.path!!.toFile(), true), group
                            )
                        )
                    }
                }
//                messageChainOf(*imageList.toTypedArray())
            } else {
                setuResponse.data.forEach { group.sendMessage(it.urls.original.toString()) }
//                PlainText(setuResponse.data[0].urls.original.toString())
            }
        } else if (setuResponse.data.isEmpty()) {
            group.sendMessage("那是什么色图？")
        } else {
            group.sendMessage("色图下载失败")
        }
        return null
    }

    fun forwardSetuMessage(text: PlainText, image: Image, contact: Contact): Message {
        return buildForwardMessage(contact) {
            add(2854196306, "色图bot", messageChainOf(text, image))
        }
    }


    suspend fun getImage(group: Group, path: Path, name: String?, isDelete: Boolean): Image {
        val newName = name ?: path.pathString
        ImgUtil.pressText(
            path.toFile(),
            File(newName),  //  覆盖式重写
            "版权所有:alice${UUID.fastUUID()}",
            Color.WHITE,
            Font("黑体", Font.BOLD, 1),
            0,
            0,
            0.0f
        )
        return StreamMessageUtil.generateImage(group, File(newName), isDelete)
    }

    suspend fun getLocalSetu(group: Group, path: String, num: Int): Message {
        val nodes: MutableList<ForwardMessage.Node> = mutableListOf()
        for (i in 0 until num) {
            val randomFile = FileUtil.getRandomFile(path, null)
            val file = Paths.get(randomFile)
            val image = getImage(group, file, "${FileUtil.localCachePath}${File.separator}${UUID.fastUUID()}.png", true)
            val node: ForwardMessage.Node =
                ForwardMessage.Node(875206320L, System.currentTimeMillis().toInt(), "Alice", image)
            nodes.add(node)
        }
        return buildForwardMessage(group) {
            addAll(nodes)
        }

    }

}