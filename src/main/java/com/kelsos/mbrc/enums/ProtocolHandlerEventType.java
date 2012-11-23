package com.kelsos.mbrc.enums;

import com.kelsos.mbrc.interfaces.IEventType;

public enum ProtocolHandlerEventType implements IEventType
{
	PROTOCOL_HANDLER_TITLE_AVAILABLE,
	PROTOCOL_HANDLER_ARTIST_AVAILABLE,
	PROTOCOL_HANDLER_ALBUM_AVAILABLE,
	PROTOCOL_HANDLER_YEAR_AVAILABLE,
	PROTOCOL_HANDLER_VOLUME_AVAILABLE,
	PROTOCOL_HANDLER_COVER_AVAILABLE,
	PROTOCOL_HANDLER_REPEAT_STATE_AVAILABLE,
	PROTOCOL_HANDLER_SHUFFLE_STATE_AVAILABLE,
	PROTOCOL_HANDLER_SCROBBLE_STATE_AVAILABLE,
	PROTOCOL_HANDLER_MUTE_STATE_AVAILABLE,
	PROTOCOL_HANDLER_PLAY_STATE_AVAILABLE,
	PROTOCOL_HANDLER_PLAYBACK_POSITION_AVAILABLE,
	PROTOCOL_HANDLER_PLAYLIST_AVAILABLE,
	PROTOCOL_HANDLER_REPLY_AVAILABLE,
	PROTOCOL_HANDLER_LYRICS_AVAILABLE,
	PROTOCOL_HANDLER_LYRICS_NOT_FOUND,
	PROTOCOL_HANDLER_REDUCE_VOLUME,
	PROTOCOL_HANDLER_HANDSHAKE_COMPLETE,
	PROTOCOL_HANDLER_NOT_ALLOWED,
	PROTOCOL_HANDLER_PLAYLIST_TRACK_REMOVE,
	PROTOCOL_HANDLER_PLUGIN_OUT_OF_DATE,
    PROTOCOL_HANDLER_RATING_RECEIVED
}
