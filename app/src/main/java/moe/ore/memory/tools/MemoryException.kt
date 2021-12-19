package moe.ore.memory.tools

import java.lang.RuntimeException

object NotFoundPidException: RuntimeException("no corresponding application found") // 应用不在运行状态或者未安装

object NoSearchResults: RuntimeException("unknown error")

object UnknownException: RuntimeException("unknown error")