import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:island/pods/call.dart';
import 'package:island/widgets/content/cloud_files.dart';
import 'package:livekit_client/livekit_client.dart';

class SpeakingRippleAvatar extends StatelessWidget {
  final bool isSpeaking;
  final double audioLevel;
  final String? pictureId;
  final double size;

  const SpeakingRippleAvatar({
    super.key,
    required this.isSpeaking,
    required this.audioLevel,
    required this.pictureId,
    this.size = 96,
  });

  @override
  Widget build(BuildContext context) {
    final avatarRadius = size / 2;
    final clampedLevel = audioLevel.clamp(0.0, 1.0);
    final rippleRadius = avatarRadius + clampedLevel * (size * 0.333);
    return TweenAnimationBuilder<double>(
      tween: Tween<double>(
        begin: avatarRadius,
        end: isSpeaking ? rippleRadius : avatarRadius,
      ),
      duration: const Duration(milliseconds: 250),
      curve: Curves.easeOut,
      builder: (context, animatedRadius, child) {
        return Stack(
          alignment: Alignment.center,
          children: [
            if (isSpeaking)
              Container(
                width: animatedRadius * 2,
                height: animatedRadius * 2,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: Colors.green.withOpacity(0.75 + 0.25 * clampedLevel),
                ),
              ),
            Container(
              width: size,
              height: size,
              alignment: Alignment.center,
              decoration: BoxDecoration(shape: BoxShape.circle),
              child: ProfilePictureWidget(fileId: pictureId, radius: size / 2),
            ),
          ],
        );
      },
    );
  }
}

class CallParticipantTile extends StatelessWidget {
  final CallParticipantLive live;

  const CallParticipantTile({super.key, required this.live});

  @override
  Widget build(BuildContext context) {
    final hasVideo =
        live.hasVideo &&
        live.remoteParticipant.trackPublications.values
            .where((pub) => pub.track != null && pub.kind == TrackType.VIDEO)
            .isNotEmpty;
    final audioLevel = live.remoteParticipant.audioLevel;

    if (hasVideo) {
      return Stack(
        fit: StackFit.loose,
        children: [
          Container(
            color: Theme.of(context).colorScheme.surfaceContainerHigh,
            child: AspectRatio(
              aspectRatio: 16 / 9,
              child: VideoTrackRenderer(
                live.remoteParticipant.trackPublications.values
                        .where((track) => track.kind == TrackType.VIDEO)
                        .first
                        .track
                    as VideoTrack,
                renderMode: VideoRenderMode.platformView,
              ),
            ),
          ),
          Positioned(
            left: 8,
            right: 8,
            bottom: 8,
            child: Text(
              live.participant.profile?.account.nick ??
                  '${'unknown'.tr()}\'s video',
              textAlign: TextAlign.center,
              style: const TextStyle(fontSize: 14, color: Colors.white),
            ),
          ),
        ],
      );
    } else {
      return SpeakingRippleAvatar(
        isSpeaking: live.isSpeaking,
        audioLevel: audioLevel,
        pictureId: live.participant.profile?.account.profile.picture?.id,
        size: 84,
      );
    }
  }
}
