"""
Outbreak Analysis with Claude AI
Analyzes outbreak signals using Claude API for epidemiological insights
"""
import azure.functions as func
import asyncpg
import json
import logging
import os
from datetime import datetime
from typing import Dict, List, Any

from shared.db import get_db_pool
from shared.config import config

logger = logging.getLogger(__name__)


def analyze_with_claude(signal_data: Dict) -> str:
    """
    Analyze outbreak signal using Claude AI
    Returns recommended actions and insights
    """
    try:
        # Only import anthropic when needed (API key may not be set in all environments)
        import anthropic

        api_key = config.ANTHROPIC_API_KEY
        if not api_key or api_key == "your_anthropic_api_key_for_outbreak_analysis":
            logger.warning("ANTHROPIC_API_KEY not configured, returning mock analysis")
            return generate_mock_analysis(signal_data)

        client = anthropic.Anthropic(api_key=api_key)

        # Build context for Claude
        signal_type = signal_data.get('signal_type', 'UNKNOWN')
        municipality = signal_data.get('municipality', 'Unknown')
        province = signal_data.get('province', 'Unknown')

        # Create detailed prompt
        prompt = f"""You are a Philippine public health epidemiologist analyzing outbreak signals from a population health surveillance system.

**Signal Details:**
- Type: {signal_type}
- Location: {municipality}, {province}
- Detection Date: {datetime.now().strftime('%Y-%m-%d')}

**Health Metrics:**
{json.dumps(signal_data, indent=2)}

Based on this data, provide:

1. **Risk Assessment**: Severity level (LOW/MEDIUM/HIGH) and justification
2. **Likely Causes**: Top 3 most probable causes for this pattern
3. **Immediate Actions**: 3-5 specific actions for Barangay Health Workers
4. **Escalation Criteria**: When to escalate to Rural Health Unit
5. **Prevention Measures**: Community-level prevention strategies

Keep your response concise, actionable, and focused on Philippine community health context.
Format as JSON with keys: risk_level, risk_justification, likely_causes (array), immediate_actions (array), escalation_criteria (string), prevention_measures (array)"""

        # Call Claude API
        message = client.messages.create(
            model="claude-sonnet-4-5-20250929",
            max_tokens=1500,
            temperature=0.3,  # Lower temperature for more consistent medical advice
            system="You are an expert Philippine public health epidemiologist. Provide evidence-based, actionable public health recommendations suitable for community health workers. Focus on practical interventions appropriate for barangay-level implementation.",
            messages=[
                {"role": "user", "content": prompt}
            ]
        )

        # Extract and parse response
        response_text = message.content[0].text

        # Try to parse as JSON
        try:
            analysis = json.loads(response_text)
        except json.JSONDecodeError:
            # If not JSON, wrap in structure
            analysis = {
                "risk_level": "MEDIUM",
                "analysis_text": response_text
            }

        logger.info(f"Claude analysis complete for {signal_type} signal in {barangay}")
        return json.dumps(analysis)

    except ImportError:
        logger.warning("anthropic module not available, returning mock analysis")
        return generate_mock_analysis(signal_data)

    except Exception as e:
        logger.error(f"Claude API error: {e}")
        return generate_mock_analysis(signal_data)


def generate_mock_analysis(signal_data: Dict) -> str:
    """Generate mock analysis when Claude API is not available"""

    signal_type = signal_data.get('signal_type', 'UNKNOWN')
    municipality = signal_data.get('municipality', 'Unknown')

    mock_analysis = {
        "risk_level": "MEDIUM",
        "risk_justification": f"Based on the {signal_type} signal patterns in {municipality}, the current risk level is assessed as MEDIUM. This is a mock analysis as Claude AI is not configured.",
        "likely_causes": [
            "Seasonal variation in health metrics",
            "Environmental factors affecting the community",
            "Changes in community health behaviors"
        ],
        "immediate_actions": [
            "Conduct door-to-door health education in affected areas",
            "Increase frequency of health monitoring scans",
            "Coordinate with Municipal Health Office for supplies",
            "Document all cases and maintain surveillance records"
        ],
        "escalation_criteria": "Escalate to RHU if: (1) Affected population exceeds 10% of barangay, (2) Severe cases requiring hospitalization emerge, (3) Pattern persists beyond 14 days",
        "prevention_measures": [
            "Community health education sessions",
            "Improved sanitation and hygiene practices",
            "Regular monitoring of high-risk individuals",
            "Strengthen referral system to health facilities"
        ],
        "note": "This is a mock analysis. Configure ANTHROPIC_API_KEY for AI-powered insights."
    }

    return json.dumps(mock_analysis)


async def get_active_signals(conn: asyncpg.Connection, limit: int = 10) -> List[Dict]:
    """Get active outbreak signals that need analysis"""

    query = """
        SELECT
            s.id,
            s.barangay_id,
            s.municipality,
            s.province,
            s.signal_type,
            s.severity,
            s.status,
            s.detected_at,
            s.affected_count,
            s.baseline_count,
            s.deviation_percentage,
            s.avg_respiratory_rate,
            s.avg_spo2,
            s.avg_heart_rate,
            s.signal_start_date,
            s.signal_end_date,
            s.days_duration,
            s.ai_recommended_actions
        FROM gold.outbreak_signals s
        WHERE s.status = 'ACTIVE'
          AND (s.ai_recommended_actions IS NULL OR s.ai_recommended_actions = '')
        ORDER BY s.severity DESC, s.detected_at DESC
        LIMIT $1
    """

    results = await conn.fetch(query, limit)
    return [dict(r) for r in results]


async def update_signal_analysis(conn: asyncpg.Connection, signal_id: int, analysis: str):
    """Update signal with AI analysis results"""

    await conn.execute("""
        UPDATE gold.outbreak_signals
        SET ai_recommended_actions = $1,
            ai_analyzed_at = $2,
            ai_analysis_completed = true,
            last_updated = $2
        WHERE id = $3
    """, analysis, datetime.utcnow(), signal_id)


async def main(req: func.HttpRequest) -> func.HttpResponse:
    """
    Outbreak Analysis Endpoint
    POST /outbreak/analyze?signal_id=<id>
    GET /outbreak/analyze (analyzes all pending signals)
    """
    logger.info("Outbreak analysis request received")

    try:
        # Get query parameters
        signal_id = req.params.get('signal_id')
        auto_analyze = req.params.get('auto', 'false').lower() == 'true'

        # Fetch signals to analyze
        pool = await get_db_pool()
        async with pool.acquire() as conn:
            if signal_id:
                # Analyze specific signal
                signal = await conn.fetchrow("""
                    SELECT s.*
                    FROM gold.outbreak_signals s
                    WHERE s.id = $1
                """, int(signal_id))

                if not signal:
                    return func.HttpResponse(
                        json.dumps({"success": False, "error": "Signal not found"}),
                        mimetype="application/json",
                        status_code=404
                    )

                signals_to_analyze = [dict(signal)]
            else:
                # Analyze all pending signals
                signals_to_analyze = await get_active_signals(conn, limit=10)

            if not signals_to_analyze:
                return func.HttpResponse(
                    json.dumps({
                        "success": True,
                        "message": "No signals pending analysis",
                        "analyzed_count": 0
                    }),
                    mimetype="application/json",
                    status_code=200
                )

            # Analyze each signal
            analyses = []
            for signal in signals_to_analyze:
                logger.info(f"Analyzing signal {signal['id']}: {signal['signal_type']} in {signal['municipality']}, {signal['province']}")

                # Prepare signal data for Claude
                signal_data = {
                    'signal_type': signal['signal_type'],
                    'municipality': signal['municipality'],
                    'province': signal['province'],
                    'severity': signal['severity'],
                    'affected_count': signal.get('affected_count'),
                    'baseline_count': signal.get('baseline_count'),
                    'deviation_percentage': signal.get('deviation_percentage'),
                    'avg_respiratory_rate': signal.get('avg_respiratory_rate'),
                    'avg_spo2': signal.get('avg_spo2'),
                    'avg_heart_rate': signal.get('avg_heart_rate'),
                    'days_duration': signal.get('days_duration')
                }

                # Get AI analysis
                analysis_json = analyze_with_claude(signal_data)

                # Store analysis
                await update_signal_analysis(conn, signal['id'], analysis_json)

                analyses.append({
                    "signal_id": signal['id'],
                    "municipality": signal['municipality'],
                    "province": signal['province'],
                    "signal_type": signal['signal_type'],
                    "analysis": json.loads(analysis_json)
                })

        # Build response
        response = {
            "success": True,
            "timestamp": int(datetime.utcnow().timestamp() * 1000),
            "analyzed_count": len(analyses),
            "analyses": analyses
        }

        logger.info(f"Outbreak analysis complete: {len(analyses)} signals analyzed")

        return func.HttpResponse(
            json.dumps(response),
            mimetype="application/json",
            status_code=200
        )

    except Exception as e:
        logger.error(f"Outbreak analysis failed: {e}")
        return func.HttpResponse(
            json.dumps({"success": False, "error": "Internal server error"}),
            mimetype="application/json",
            status_code=500
        )
