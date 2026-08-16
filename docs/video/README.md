# AI Support System - Demo Video

## Platform Walkthrough (Animated)

The animated walkthrough below cycles through the key screens of the AI Support System, demonstrating the end-to-end flow from ticket creation through AI-powered resolution.

![Platform Walkthrough](demo-video.gif)

## Walkthrough Sequence

The GIF presents the following screens in order:

| # | Screen | What It Shows |
| - | ------ | ------------- |
| 1 | **Login** | JWT-authenticated sign-in with role-based access |
| 2 | **Ticket Submission** | Customer creates a new support ticket |
| 3 | **Customer Workspace** | Customer views their submitted tickets and statuses |
| 4 | **Redpanda Console** | Kafka `ticket-created` event payload in real-time |
| 5 | **Agent Queue** | Smart-routed ticket lands in the correct agent queue |
| 6 | **AI Insights** | Sentiment (ANGRY), urgency score, and intent classification |
| 7 | **AI Draft** | LLM-generated response draft citing knowledge articles |
| 8 | **RAG Context** | Retrieved knowledge base articles with similarity scores |
| 9 | **Ticket Timeline** | Full AI pipeline status from submission to resolution |
| 10 | **Knowledge Base** | Admin view of published and draft articles |
| 11 | **Admin Dashboard** | Operations center with AI governance metrics |

## Recording Details

- **Format:** Animated GIF (11 frames, 2.5 seconds per frame)
- **Resolution:** 1280px wide (aspect ratio preserved per screenshot)
- **File size:** ~1.2 MB
- **Loop:** Infinite

