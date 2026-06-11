"""OpenAI-compatible LLM provider.

Connects to any OpenAI-compatible chat completion endpoint.
Currently targets the internal test-automation-ai-api extension endpoint.
"""

import httpx

from analyzer.llm.base import LLMProvider, LLMResponse


class OpenAICompatibleLLMProvider(LLMProvider):
    """LLM provider for OpenAI-compatible HTTP endpoints."""

    def __init__(
        self,
        base_url: str,
        model: str,
        api_key: str = "",
        endpoint_path: str = "/api/v1/extension/send",
        timeout_seconds: int = 60,
    ) -> None:
        self._base_url = base_url.rstrip("/")
        self._model = model
        self._api_key = api_key
        self._endpoint_path = endpoint_path
        self._timeout_seconds = timeout_seconds

    @property
    def provider_name(self) -> str:
        return "openai_compatible"

    @property
    def model_name(self) -> str:
        return self._model

    async def complete(
        self, prompt: str, system_prompt: str | None = None
    ) -> LLMResponse:
        messages = []
        if system_prompt:
            messages.append({"role": "system", "content": system_prompt})
        messages.append({"role": "user", "content": prompt})

        body = {
            "messages": messages,
            "temperature": 0,
            "max_tokens": 2000,
        }

        url = f"{self._base_url}{self._endpoint_path}"

        async with httpx.AsyncClient(timeout=self._timeout_seconds) as client:
            response = await client.post(url, json=body)
            response.raise_for_status()
            data = response.json()

        content = data["choices"][0]["message"]["content"]
        usage = data.get("usage") or {}

        return LLMResponse(
            content=content,
            model=self._model,
            provider=self.provider_name,
            input_tokens=usage.get("prompt_tokens", 0),
            output_tokens=usage.get("completion_tokens", 0),
        )

    async def health_check(self) -> bool:
        try:
            await self.complete(prompt="ping")
            return True
        except Exception:
            return False
