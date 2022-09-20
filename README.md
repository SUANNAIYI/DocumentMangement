# DocumentManageServer

## 简介

此项目为文件整编系统，项目使用Vue + SpringBoot 2 + Mybatis-plus框架搭建。

涉及的技术栈有Shiro, JWT token, Redis, Knife4j, WebSocket等，同时集成了KKFileView与OnlyOffice。

## 功能

支持的功能有文件上传、下载、预览、编辑、共享、发送、审核、发布，能够查看文件编辑的版本记录，文件流转的流转过程，文件流转时在用户间发送消息提醒。

目前支持上传的文件类型有：

- 文本：.txt, .doc, .docx, .pptx, .xlsx
- 图片：.png, .gif, .jpg, .jpeg
- 视频：.mp4
- 音频：.mp3

目前支持编辑的文件类型有全套Office文件，需在本地部署OnlyOffice服务。

## 部署步骤
OnlyOffice在线编辑：

<<<<<<< HEAD
OnlyOffice在线编辑：

=======
>>>>>>> f4cd336627c81453419a548366e8c33b5d1d63e3
下载地址：[Download ONLYOFFICE Docs](https://www.onlyoffice.com/download-docs.aspx?from=document-editor#docs-community)

安装教程：[Installing ONLYOFFICE Docs for Windows on a local server - ONLYOFFICE](https://helpcenter.onlyoffice.com/installation/docs-community-install-windows.aspx)

Api接口使用教程：[ONLYOFFICE Api Documentation](https://api.onlyoffice.com/editors)

KKFileView文件预览：

Gitee代码仓：[kkFileView下载地址](https://gitee.com/kekingcn/file-online-preview)
