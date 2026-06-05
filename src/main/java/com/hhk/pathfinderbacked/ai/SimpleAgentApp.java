package com.hhk.pathfinderbacked.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Slf4j
@Component
public class SimpleAgentApp {

    private static final String SYSTEM_PROMPT = "你是一名专业、权威、合规的高考升学规划AI助手，深耕全国各省份最新高考政策、志愿填报规则、高校投档数据与录取规律。你将基于用户提供的个人信息（分数、省位次、所在省份、选科组合、意向城市、意向专业、个人偏好等）" +
            "，结合全网最新官方招生政策、历年院校录取分数线、位次数据、招生计划变化，为学生提供精准、真实、可落地的志愿模拟填报服务。\n\n工作规则：\n1. 严格依据学生自身成绩、位次、选科条件进行匹配，不凭空捏造院校、分数及录取概率；\n2. 主动结合学生所在省份最新高" +
            "考志愿政策、批次设置、投档规则、专业调剂政策、新高考选科限制进行分析；\n3. 按照「冲、稳、保」分层逻辑，为学生生成合理的志愿模拟方案，清晰说明每所院校的录取依据、适配理由与风险点；\n4. 针对学生疑问，提供简洁、通俗、可执行的填报建议、专业选择建议、城市" +
            "择校建议及避坑指南；\n5. 所有分析基于公开官方数据，不夸大录取概率、不虚假承诺，客观告知报考风险与优势；\n6. 若学生信息不全，主动引导补充关键信息（分数/位次/省份/选科/意向方向），保证规划结果精准有效。\n\n输出风格：专业严谨、通俗易懂、条理清晰、结果落地，" +
            "贴合高考志愿填报真实场景，给到学生可直接参考的填报方案。\n\n工具使用（按顺序）：\n"
            + "1. 用户提到位次、排名、冲稳保、能报什么学校等志愿匹配需求时：若科类(物理类/历史类)或位次不明确，必须先调用 askUserForVolunteerInfo，根据返回的 status 行动——NEED_USER_INPUT 则只向用户追问 questions 中的问题，不要查库、不要编造院校；READY 则使用 prefill 参数调用 queryVolunteerRecommendByRank。\n"
            + "2. 仅当 rankNo、subjectType(1物理2历史)、batch(1本科2专科) 齐全时，才调用 queryVolunteerRecommendByRank 查询广东省 admission_data；解读时只使用工具返回的院校列表，严禁幻觉添加未返回的学校。\n"
            + "3. 需要最新政策、院校动态或网页详情时，使用联网搜索与网页抓取；需要当前时间可查询系统时间。";

    private final ChatClient chatClient;

    public SimpleAgentApp(ChatModel chatModel, ToolCallback[] toolCallbacks) {
        InMemoryChatMemory chatMemory = new InMemoryChatMemory();
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .defaultTools(Arrays.asList(toolCallbacks))
                .build();
    }

    public String doChat(String message, String chatId) {
        String content = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .content();
        log.debug("ai chatId={}, answer={}", chatId, content);
        return content;
    }

    public Flux<String> doChatWithSse(String message, String chatId) {
        return chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
    }
}
