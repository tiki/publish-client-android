package com.mytiki.publish.client.auth

import java.io.Serializable

enum class TokenProviderEnum : Serializable {
  GOOGLE,
  TIKI;

  companion object {
    fun fromString(name: String) = entries.firstOrNull() { name == it.toString() }
  }

  override fun toString() = this.name
}
