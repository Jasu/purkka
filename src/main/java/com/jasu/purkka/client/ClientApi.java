package com.jasu.purkka.client;

import com.jasu.purkka.http.Endpoint;
import com.jasu.purkka.http.HttpException;
import net.minecraft.client.resources.language.I18n;

public final class ClientApi {
  @Endpoint(path="translate/%s")
  public String translate(String s) {
    return I18n.get(s);
  }
}
