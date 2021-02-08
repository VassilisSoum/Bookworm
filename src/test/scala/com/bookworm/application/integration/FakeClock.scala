package com.bookworm.application.integration

import java.time.{Clock, Instant, ZoneId}

class FakeClock extends Clock {

  var zoneId: ZoneId = ZoneId.of("UTC")
  var current: Instant = Instant.now().atZone(zoneId).toInstant

  override def getZone: ZoneId = zoneId

  override def withZone(zoneId: ZoneId): Clock = {
    this.zoneId = zoneId
    this
  }

  override def instant(): Instant =
    current.atZone(zoneId).toInstant
}
