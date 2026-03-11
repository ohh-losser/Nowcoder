package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


//铭感词过滤
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {

        try(
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        )
        {
            String keyword;
            while( (keyword = br.readLine()) != null) {
                this.addKeyword(keyword);
            }

        }catch (Exception e){
            logger.error("加载敏感词文件失败");
        }

    }

    // 添加敏感词的方法
    private void addKeyword(String keyword) {
        //从根节点出发，准备开始向下“挖”。
        TrieNode tempNode = rootNode;
        //把敏感词拆解为一个个字符，从头到尾遍历。
        for (int i = 0; i < keyword.length(); i++) {
            Character c = keyword.charAt(i);
            //这是在询问：“当前这个节点下，有没有通往字符 c 的路？”
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                //如果发现没有这条路，就新建一个节点 (new TrieNode())，并把这条路挂上去 (tempNode.addSubNode(c, subNode))。
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            //这一步非常重要！把指针向下移动，准备处理下一个字符。
            tempNode = subNode;
        }
        //循环结束后，最后一个节点代表关键词的最后一个字符，必须标记为“结束”，否则没法判断它是关键词还是另一个词的前缀。
        tempNode.setKeywordEnd(true);
    }

    // 过滤 铭感词 拼接字符串
    public String filter(String text) {
        //如果文本为空，直接返回。

        System.out.println("开始过滤");

        if (StringUtils.isBlank(text)) {
            return null;
        }
        //从根节点出发，准备开始向下“挖”。 指针1
        TrieNode tempNode = rootNode;

        // 记录上一个匹配到的敏感词的结束位置 指针2
        int begin = 0;
        // 记录当前正在处理的字符位置 指针3
        int position = 0;

        // 用于构建过滤后的文本
        StringBuilder sb = new StringBuilder();

        // 开始遍历文本
        while (position < text.length()) {
            Character c = text.charAt(position);

            //过滤符号
            if (isSymbol(c)) {
                //若指针1处于根节点，将此字符加入结果，让指针2向下走一步
                if(tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                //无论符号在开头或中间，指针3都向下走一步
                position++;
                continue;
            }

            //不是符号 检查下个节点
            //这是在询问：“当前这个节点下，有没有通往字符 c 的路？”
            tempNode = tempNode.getSubNode(c);
            //如果发现没有通往字符 c 的路，说明当前位置之前的字符不是敏感词的前缀，直接把当前字符加入结果。
            if (tempNode == null) {
                // 以begin开头的字符串不是铭感词，加入结果
                sb.append(text.charAt(begin));
                // 指针后移 进入下一个位置
                position = ++begin;
                // 重置指针 重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词，将begin~posion字符串替换掉
                //System.out.println("检测到敏感词，触发替换，当前sb内容: " + sb.toString());
                sb.append(REPLACEMENT);
                // 指针后移 进入下一个位置
                position++;
                begin = position;

                tempNode = rootNode;
            } else {
                // 指针后移 继续向下“挖”
                position++;
            }


        }

        // 将最后一个位置的字符加入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

        // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80-0x9FFF 东亚文字范围
        return !Character.isLetterOrDigit(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //前缀树
    private class TrieNode {
        //关键词结束标识
        private boolean isKeywordEnd = false;

        //子节点 key为下级字符，value为下级节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点的方法
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }

}
