# Privacy

Dim Veil is offline by design. It does not request Internet access and contains no advertising SDK, analytics, account system, cloud synchronization, crash-reporting service, telemetry, or tracking identifier.

Only local display preferences (selected mode and dim depth) are saved with Android DataStore. The app never reads screen content, user input, accessibility-node content, notifications, or other application data.

The optional accessibility service is limited to rendering an overlay window; its configuration disables content retrieval and gesture performance. The optional Shizuku integration is user-authorized and only manages Dim Veil's own accessibility-service entry. Details are in [permissions.md](permissions.md) and [accessibility.md](accessibility.md).

These statements are part of the project's security boundary. Any proposal to add network access, data collection, automation, or content inspection requires public design discussion and security review.
