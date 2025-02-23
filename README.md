**This fully client side mod adds a command that allows you to communicate with ChatGPT while playing Minecraft**. It also works while you are playing on a Server. With the added ```/ask``` command, you can ask any question you want and receive an answer in the Minecraft chat:



![Question](https://cdn.modrinth.com/data/cached_images/54b8596cf84c75b9dd4c72032d4aa8ed5d9e3a10_0.webp)
![Answer](https://cdn.modrinth.com/data/cached_images/9e27c6149c1984ee950ba3a13b36288035590672_0.webp)

### Download
________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________

You can download this mod [here](https://modrinth.com/mod/aichat)

### Setup Guide
________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________

**To use the mod**, you first need to type ```/config OPENAI_API_KEY <Your API key>``` to connect your client to the OpenAI API. You can obtain your API key [here](https://platform.openai.com/settings/organization/api-keys).

![API Key](https://cdn.modrinth.com/data/cached_images/c292615edd009868aa2df0fff9469662201e183a_0.webp)

Make sure you have some **funds in your OpenAI account**. The default model (```gpt-3.5-turbo```) doesn’t really require any money, but you still need a minimum balance of $0.01 to use the API. You can add funds to your account [here](https://platform.openai.com/settings/organization/billing/overview)

### Configuration
________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________

If you want to use a model other than ```gpt-3.5-turbo``` (e.g., ```gpt-4o```), you can configure it by typing ```/config AI_MODEL <AI model you want to use>```. Keep in mind that better models are also more expensive. You can view all available models [here](https://platform.openai.com/settings/organization/limits).

![Model](https://cdn.modrinth.com/data/cached_images/1a529a72f6a973dae11a7787bcf87a936ecb3833_0.webp)

To change the role of ChatGPT, type ```/config ROLE_OF_THE_AI <Role the AI should play>```
. The default role is:
_"You are an assistant implemented in a Minecraft mod to help players. You provide short answers with a maximum length of 4 short sentences."_

![Role](https://cdn.modrinth.com/data/cached_images/fe3e94fe4d27167f38bfea3ab32a2c6b415a562d_0.webp)
